package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.bookmark.BookmarkFolderTimelineRequest
import work.socialhub.kxweb.entity.bookmark.BookmarkRequest
import work.socialhub.kxweb.entity.bookmark.GetBookmarksRequest
import work.socialhub.kxweb.entity.bookmark.GetBookmarksResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.model.MutationResult
import kotlin.js.JsExport

@JsExport
interface BookmarkResource {

    suspend fun getBookmarks(request: GetBookmarksRequest): Response<GetBookmarksResponse>

    @JsExport.Ignore
    fun getBookmarksBlocking(request: GetBookmarksRequest): Response<GetBookmarksResponse>

    suspend fun bookmark(request: BookmarkRequest): Response<MutationResult>

    @JsExport.Ignore
    fun bookmarkBlocking(request: BookmarkRequest): Response<MutationResult>

    suspend fun unbookmark(request: BookmarkRequest): Response<MutationResult>

    @JsExport.Ignore
    fun unbookmarkBlocking(request: BookmarkRequest): Response<MutationResult>

    suspend fun getBookmarkFolderTimeline(
        request: BookmarkFolderTimelineRequest
    ): Response<GetBookmarksResponse>

    @JsExport.Ignore
    fun getBookmarkFolderTimelineBlocking(
        request: BookmarkFolderTimelineRequest
    ): Response<GetBookmarksResponse>
}
