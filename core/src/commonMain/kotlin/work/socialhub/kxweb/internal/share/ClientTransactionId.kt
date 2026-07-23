package work.socialhub.kxweb.internal.share

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import work.socialhub.khttpclient.HttpRequest
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.internal.share.InternalUtility.USER_AGENT
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.isGuest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.util.Sha256Util
import kotlin.concurrent.Volatile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Generates the per-request x-client-transaction-id used by X web.
 *
 * The verification key and animation data are derived from the authenticated
 * X home page and its current ondemand.s bundle, following QuaX's implementation.
 */
object ClientTransactionId {

    private const val HOME_URL = "https://x.com/home"
    private const val ONDEMAND_URL_TEMPLATE =
        "https://abs.twimg.com/responsive-web/client-web/ondemand.s.%sa.js"
    private const val SECRET = "obfiowerehiring"
    private const val TIME_EPOCH_OFFSET_SECONDS = 1_682_924_400L
    private const val ADDITIONAL_RANDOM_NUMBER = 3
    private const val CACHE_DURATION_SECONDS = 3600L

    @Volatile
    private var cachedPair: TransactionPair? = null

    @Volatile
    private var cacheTimestamp: Long = 0L

    private val refreshMutex = Mutex()

    internal data class TransactionPair(
        val keyBytes: ByteArray,
        val animationKey: String,
    )

    /**
     * Generate an ID for the exact HTTP method and URL path being requested.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun generate(method: String = "GET", path: String = ""): String {
        val pair = checkNotNull(cachedPair) {
            "Client transaction data is unavailable"
        }

        return try {
            val timeNow = Clock.System.now().epochSeconds - TIME_EPOCH_OFFSET_SECONDS
            val timeBytes = encodeTimestampLittleEndian(timeNow)
            val hashInput = "$method!$path!$timeNow$SECRET${pair.animationKey}"
            val hashBytes = Sha256Util.hash(hashInput.encodeToByteArray())
            val payload = pair.keyBytes +
                    timeBytes +
                    hashBytes.copyOfRange(0, minOf(16, hashBytes.size)) +
                    byteArrayOf(ADDITIONAL_RANDOM_NUMBER.toByte())

            val xorMask = Random.nextBytes(1)[0]
            val result = byteArrayOf(xorMask) + ByteArray(payload.size) { index ->
                (payload[index].toInt() xor xorMask.toInt()).toByte()
            }
            Base64.encode(result).trimEnd('=')
        } catch (e: Exception) {
            throw IllegalStateException("Failed to generate client transaction ID", e)
        }
    }

    /**
     * Refresh transaction pair data from the authenticated X web application.
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun refreshPairData(config: XWebConfig? = null) {
        val now = Clock.System.now().epochSeconds
        if (cachedPair != null && (now - cacheTimestamp) < CACHE_DURATION_SECONDS) {
            return
        }

        refreshMutex.withLock {
            val refreshedAt = Clock.System.now().epochSeconds
            if (cachedPair != null &&
                (refreshedAt - cacheTimestamp) < CACHE_DURATION_SECONDS
            ) {
                return@withLock
            }

            try {
                val homeRequest = createHomeRequest(config)
                val homeResponse = homeRequest.get()
                if (homeResponse.status !in 200..299) return@withLock
                val homeHtml = homeResponse.stringBody

                val verification = Regex(
                    """<meta[^>]*name=["']twitter-site-verification["'][^>]*content=["']([^"']+)["']"""
                ).find(homeHtml)?.groupValues?.get(1) ?: return@withLock

                val chunkIndex = Regex(""",(\d+):["']ondemand\.s["']""")
                    .find(homeHtml)
                    ?.groupValues
                    ?.get(1)
                    ?: return@withLock
                val chunkHash = Regex(""",$chunkIndex:["']([0-9a-f]+)["']""")
                    .find(homeHtml)
                    ?.groupValues
                    ?.get(1)
                    ?: return@withLock

                val ondemandResponse = createRequest(config)
                    .url(ONDEMAND_URL_TEMPLATE.replace("%s", chunkHash))
                    .header("user-agent", USER_AGENT)
                    .get()
                if (ondemandResponse.status !in 200..299) return@withLock

                val indices = Regex("""\(\w+\[(\d{1,2})],\s*16\)""")
                    .findAll(ondemandResponse.stringBody)
                    .map { it.groupValues[1].toInt() }
                    .toList()
                if (indices.size < 2) return@withLock

                val keyBytes = Base64.decode(verification)
                val animationKey = computeAnimationKey(
                    keyBytes = keyBytes,
                    rowIndex = indices.first(),
                    keyByteIndices = indices.drop(1),
                    homeHtml = homeHtml,
                )
                cachedPair = TransactionPair(keyBytes, animationKey)
                cacheTimestamp = refreshedAt
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep the previous cached pair, if any.
            }
        }
    }

    fun isPairDataAvailable(): Boolean = cachedPair != null

    internal fun clearCache() {
        cachedPair = null
        cacheTimestamp = 0L
    }

    internal fun setPairData(keyBytes: ByteArray, animationKey: String) {
        cachedPair = TransactionPair(keyBytes, animationKey)
        cacheTimestamp = Clock.System.now().epochSeconds
    }

    internal fun createRequest(config: XWebConfig?): HttpRequest {
        return if (config != null) {
            httpRequest(config).setTimeouts(config)
        } else {
            HttpRequest()
        }
    }

    internal fun createHomeRequest(config: XWebConfig?): HttpRequest {
        return createRequest(config)
            .url(HOME_URL)
            .header("accept-language", "en-US,en;q=0.9")
            .header("cache-control", "no-cache")
            .header("referer", "https://x.com/")
            .header("user-agent", USER_AGENT)
            .header("x-twitter-active-user", "yes")
            .header("x-twitter-client-language", "en")
            .also { request ->
                val cookie = config
                    ?.takeUnless(::isGuest)
                    ?.let(::transactionCookie)
                if (!cookie.isNullOrBlank()) {
                    request.header("cookie", cookie)
                }
            }
    }

    private fun transactionCookie(config: XWebConfig): String? {
        return config.cookieString ?: if (
            config.authToken != null &&
            config.csrfToken != null
        ) {
            "auth_token=${config.authToken}; ct0=${config.csrfToken}"
        } else {
            null
        }
    }

    private fun computeAnimationKey(
        keyBytes: ByteArray,
        rowIndex: Int,
        keyByteIndices: List<Int>,
        homeHtml: String,
    ): String {
        val frames = Regex(
            """<svg[^>]*id=["']loading-x-anim-[^"']+["'][^>]*>[\s\S]*?</svg>""",
            RegexOption.IGNORE_CASE,
        ).findAll(homeHtml).map { it.value }.toList()
        val frame = frames[unsigned(keyBytes[5]) % 4]
        val paths = Regex(
            """<path[^>]*\sd=["']([^"']+)["']""",
            RegexOption.IGNORE_CASE,
        ).findAll(frame).map { it.groupValues[1] }.toList()
        val animationPath = paths[1].substringAfter("C")

        val rows = animationPath.split("C").mapNotNull { segment ->
            val values = segment.replace(Regex("""[^\d]+"""), " ")
                .trim()
                .split(Regex("""\s+"""))
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
            values.takeIf { it.isNotEmpty() }
        }

        val frameRowIndex = unsigned(keyBytes[rowIndex]) % 16
        val frameTimeProduct = keyByteIndices.fold(1) { result, index ->
            result * (unsigned(keyBytes[index]) % 16)
        }
        val frameTime = jsRound(frameTimeProduct / 10.0) * 10
        return animate(rows[frameRowIndex], frameTime / 4096.0)
    }

    private fun animate(frames: List<Int>, targetTime: Double): String {
        val fromColor = listOf(
            frames[0].toDouble(),
            frames[1].toDouble(),
            frames[2].toDouble(),
            1.0,
        )
        val toColor = listOf(
            frames[3].toDouble(),
            frames[4].toDouble(),
            frames[5].toDouble(),
            1.0,
        )
        val toRotation = solve(frames[6].toDouble(), 60.0, 360.0, true)
        val curves = frames.drop(7).mapIndexed { index, value ->
            solve(value.toDouble(), if (index % 2 != 0) -1.0 else 0.0, 1.0, false)
        }
        val value = Cubic(curves).getValue(targetTime)
        val color = interpolate(fromColor, toColor, value)
            .map { it.coerceIn(0.0, 255.0) }
        val radians = interpolate(listOf(0.0), listOf(toRotation), value)[0] * PI / 180.0
        val matrix = listOf(cos(radians), -sin(radians), sin(radians), cos(radians))

        val values = mutableListOf<String>()
        color.dropLast(1).forEach { values.add(it.roundToInt().toString(16)) }
        matrix.forEach { matrixValue ->
            val hex = floatToHex(abs(roundTo2(matrixValue)))
            values.add(if (hex.startsWith(".")) "0${hex.lowercase()}" else hex.ifEmpty { "0" })
        }
        values.add("0")
        values.add("0")
        return values.joinToString("").replace(".", "").replace("-", "")
    }

    private fun solve(value: Double, min: Double, max: Double, rounding: Boolean): Double {
        val result = value * (max - min) / 255.0 + min
        return if (rounding) floor(result) else roundTo2(result)
    }

    private fun interpolate(from: List<Double>, to: List<Double>, fraction: Double): List<Double> {
        return from.indices.map { index ->
            from[index] * (1 - fraction) + to[index] * fraction
        }
    }

    private fun floatToHex(value: Double): String {
        val integer = value.toInt()
        val result = StringBuilder()
        if (integer > 0) {
            result.append(integer.toString(16))
        }

        var fraction = value - integer
        if (fraction == 0.0) return result.toString()
        result.append('.')
        repeat(16) {
            if (fraction == 0.0) return@repeat
            fraction *= 16
            val digit = fraction.toInt()
            fraction -= digit
            result.append(digit.toString(16))
        }
        return result.toString()
    }

    private fun roundTo2(value: Double): Double = (value * 100).roundToInt() / 100.0

    private fun jsRound(value: Double): Int {
        val lower = floor(value)
        return if (value - lower >= 0.5) lower.toInt() + 1 else lower.toInt()
    }

    private fun unsigned(value: Byte): Int = value.toInt() and 0xFF

    private fun encodeTimestampLittleEndian(epochSeconds: Long): ByteArray {
        return byteArrayOf(
            (epochSeconds and 0xFF).toByte(),
            ((epochSeconds shr 8) and 0xFF).toByte(),
            ((epochSeconds shr 16) and 0xFF).toByte(),
            ((epochSeconds shr 24) and 0xFF).toByte(),
        )
    }

    private class Cubic(
        private val curves: List<Double>,
    ) {
        fun getValue(time: Double): Double {
            if (time <= 0.0) {
                val gradient = when {
                    curves[0] > 0.0 -> curves[1] / curves[0]
                    curves[1] == 0.0 && curves[2] > 0.0 -> curves[3] / curves[2]
                    else -> 0.0
                }
                return gradient * time
            }
            if (time >= 1.0) {
                val gradient = when {
                    curves[2] < 1.0 -> (curves[3] - 1.0) / (curves[2] - 1.0)
                    curves[2] == 1.0 && curves[0] < 1.0 ->
                        (curves[1] - 1.0) / (curves[0] - 1.0)
                    else -> 0.0
                }
                return 1.0 + gradient * (time - 1.0)
            }

            var start = 0.0
            var end = 1.0
            var middle = 0.0
            repeat(100) {
                middle = (start + end) / 2
                val estimate = calculate(curves[0], curves[2], middle)
                if (abs(time - estimate) < 0.00001) {
                    return calculate(curves[1], curves[3], middle)
                }
                if (estimate < time) start = middle else end = middle
            }
            return calculate(curves[1], curves[3], middle)
        }

        private fun calculate(a: Double, b: Double, middle: Double): Double {
            return 3.0 * a * (1 - middle) * (1 - middle) * middle +
                    3.0 * b * (1 - middle) * middle * middle +
                    middle * middle * middle
        }
    }
}
