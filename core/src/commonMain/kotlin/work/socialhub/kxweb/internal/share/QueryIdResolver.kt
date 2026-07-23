package work.socialhub.kxweb.internal.share

import kotlinx.coroutines.CancellationException
import work.socialhub.khttpclient.HttpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.USER_AGENT
import kotlin.concurrent.Volatile

object QueryIdResolver {

    private var cachedIds: Map<String, String>? = null
    @Volatile
    private var fetchCount: Int = 0
    private const val MAX_USES_BEFORE_REFRESH = 1000

    internal fun cachedId(operationName: String): String? {
        val cached = cachedIds?.get(operationName)
        if (cached != null && fetchCount < MAX_USES_BEFORE_REFRESH) {
            fetchCount++
            return cached
        }
        return null
    }

    suspend fun resolve(
        operationName: String,
        fallback: String,
        forceRefresh: Boolean = false,
    ): String = resolveWithRefresh(operationName, fallback, forceRefresh) {
        refreshFromBundles()
    }

    internal suspend fun resolveWithRefresh(
        operationName: String,
        fallback: String,
        forceRefresh: Boolean = false,
        refresh: suspend () -> Unit,
    ): String {
        if (!forceRefresh) {
            cachedId(operationName)?.let { return it }
        }

        return try {
            refresh()
            cachedId(operationName) ?: fallback
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fallback
        }
    }

    fun invalidateCache() {
        cachedIds = null
        fetchCount = 0
    }

    private suspend fun refreshFromBundles() {
        val html = fetchPage("https://x.com")
        val scriptUrls = extractScriptUrls(html)
        val ids = mutableMapOf<String, String>()

        for (scriptUrl in scriptUrls) {
            try {
                val jsContent = fetchPage(scriptUrl)
                val extracted = extractQueryIds(jsContent)
                ids.putAll(extracted)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // skip failed bundle
            }
        }

        if (ids.isNotEmpty()) {
            cachedIds = ids
            fetchCount = 0
        }
    }

    private suspend fun fetchPage(url: String): String {
        val request = HttpRequest()
            .url(url)
            .header("user-agent", USER_AGENT)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")

        val response = request.get()
        return response.stringBody
    }

    internal fun extractScriptUrls(html: String): List<String> {
        val pattern = Regex("""src="(https://abs\.twimg\.com/responsive-web/client-web[^"]*\.js)"""")
        return pattern.findAll(html).map { it.groupValues[1] }.toList()
    }

    internal fun extractQueryIds(jsContent: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val pattern = Regex("""\{queryId:"([^"]+)",operationName:"([^"]+)"""")
        for (match in pattern.findAll(jsContent)) {
            val queryId = match.groupValues[1]
            val operationName = match.groupValues[2]
            result[operationName] = queryId
        }
        return result
    }

    internal fun setCachedIds(ids: Map<String, String>) {
        cachedIds = ids
        fetchCount = 0
    }
}
