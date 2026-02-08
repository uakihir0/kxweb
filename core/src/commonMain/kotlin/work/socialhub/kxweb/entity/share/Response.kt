package work.socialhub.kxweb.entity.share

import kotlin.js.JsExport

@JsExport
data class Response<T>(
    val data: T,
    val json: String,
)

@JsExport
data class ResponseUnit(
    val json: String,
)
