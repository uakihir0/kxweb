package work.socialhub.kxweb.entity.bookmark

import kotlin.js.JsExport

@JsExport
class BookmarkFolderTimelineRequest {
    var folderId: String? = null
    var count: Int = 20
    var cursor: String? = null
}
