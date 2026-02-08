package work.socialhub.kxweb.domain

/**
 * GraphQL Query IDs for X (Twitter) web API.
 *
 * These query IDs are extracted from X's frontend bundles and may change
 * without notice. When the API breaks, these IDs may need to be updated.
 *
 * Reference: https://github.com/steipete/bird (query-ids.json)
 */
object QueryId {
    const val SEARCH_TIMELINE = "6AAys3t42mosm_yTI_QENg"
    const val TWEET_DETAIL = "_NvJCnIjOW__EP5-RF197A"
    const val TWEET_RESULT_BY_REST_ID = "0aTrQMKgj95K791yXeNDRA"
}
