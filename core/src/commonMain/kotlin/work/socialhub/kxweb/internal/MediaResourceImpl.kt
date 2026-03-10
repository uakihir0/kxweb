package work.socialhub.kxweb.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.MediaResource
import work.socialhub.kxweb.entity.media.UploadMediaRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.BEARER_TOKEN
import work.socialhub.kxweb.internal.share.InternalUtility.USER_AGENT
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.withCookieHeaders
import work.socialhub.kxweb.model.UploadMediaResult
import work.socialhub.kxweb.util.toBlocking
import kotlin.math.min

class MediaResourceImpl(
    private val config: XWebConfig
) : MediaResource {

    companion object {
        private const val UPLOAD_URL = "https://upload.x.com/i/media/upload.json"
        private const val CHUNK_SIZE = 5 * 1024 * 1024 // 5MB
    }

    override suspend fun uploadMedia(
        request: UploadMediaRequest
    ): Response<UploadMediaResult> {
        val data = request.data ?: throw InternalUtility.handleError(
            null, body = "Media data is required"
        )
        val mimeType = request.mimeType ?: "image/jpeg"

        // Step 1: INIT
        val initResponse = initUpload(data.size, mimeType)
        val mediaId = initResponse.mediaIdString
            ?: return Response(UploadMediaResult(success = false, error = "Failed to init upload"), "")

        // Step 2: APPEND (chunked)
        var offset = 0
        var segmentIndex = 0
        while (offset < data.size) {
            val end = min(offset + CHUNK_SIZE, data.size)
            val chunk = data.copyOfRange(offset, end)
            appendUpload(mediaId, segmentIndex, chunk)
            offset = end
            segmentIndex++
        }

        // Step 3: FINALIZE
        val finalizeResponse = finalizeUpload(mediaId)

        // Step 4: Set alt text if provided
        request.altText?.let { alt ->
            setAltText(mediaId, alt)
        }

        return Response(
            UploadMediaResult(
                success = true,
                mediaId = finalizeResponse.mediaIdString,
            ),
            "",
        )
    }

    override fun uploadMediaBlocking(
        request: UploadMediaRequest
    ): Response<UploadMediaResult> = toBlocking { uploadMedia(request) }

    private suspend fun initUpload(totalBytes: Int, mimeType: String): MediaUploadResponse {
        val httpRequest = httpRequest(config)
            .url(UPLOAD_URL)
            .setTimeouts(config)
            .param("command", "INIT")
            .param("total_bytes", totalBytes.toString())
            .param("media_type", mimeType)
            .withCookieHeaders(config)

        val response = httpRequest.post()

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, response.stringBody)
        }

        return fromJson<MediaUploadResponse>(response.stringBody)
    }

    private suspend fun appendUpload(mediaId: String, segmentIndex: Int, chunk: ByteArray) {
        val httpRequest = httpRequest(config)
            .url(UPLOAD_URL)
            .setTimeouts(config)
            .param("command", "APPEND")
            .param("media_id", mediaId)
            .param("segment_index", segmentIndex.toString())
            .file("media_data", "blob", chunk)
            .withCookieHeaders(config)

        val response = httpRequest.post()

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, response.stringBody)
        }
    }

    private suspend fun finalizeUpload(mediaId: String): MediaUploadResponse {
        val httpRequest = httpRequest(config)
            .url(UPLOAD_URL)
            .setTimeouts(config)
            .param("command", "FINALIZE")
            .param("media_id", mediaId)
            .withCookieHeaders(config)

        val response = httpRequest.post()

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, response.stringBody)
        }

        return fromJson<MediaUploadResponse>(response.stringBody)
    }

    private suspend fun setAltText(mediaId: String, altText: String) {
        val body = """{"media_id":"$mediaId","alt_text":{"text":"$altText"}}"""

        val httpRequest = httpRequest(config)
            .url("https://upload.x.com/i/media/upload.json")
            .setTimeouts(config)
            .header("content-type", "application/json")
            .json(body)
            .withCookieHeaders(config)

        httpRequest.post()
    }

    @Serializable
    private data class MediaUploadResponse(
        @SerialName("media_id_string")
        val mediaIdString: String? = null,
        @SerialName("processing_info")
        val processingInfo: JsonElement? = null,
    )
}
