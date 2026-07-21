package work.socialhub.kxweb.domain

/**
 * GraphQL Query IDs for X (Twitter) web API.
 *
 * These query IDs are extracted from X's frontend bundles and may change
 * without notice. When the API breaks, these IDs may need to be updated.
 *
 * Reference: https://github.com/steipete/bird (query-ids.json)
 * Reference: https://github.com/taelin-app/twitter-cli (client.py)
 */
object QueryId {
    // Search
    const val SEARCH_TIMELINE = "6AAys3t42mosm_yTI_QENg"

    // Tweet
    const val TWEET_DETAIL = "_NvJCnIjOW__EP5-RF197A"
    const val TWEET_RESULT_BY_REST_ID = "0aTrQMKgj95K791yXeNDRA"

    // Home Timeline
    const val HOME_TIMELINE = "c-CzHF1LboFilMpsx4ZCrQ"
    const val HOME_LATEST_TIMELINE = "BKB7oi212Fi7kQtCBGE4zA"

    // User
    const val USER_BY_SCREEN_NAME = "2qvSHpkWTMS9i0zJAwDNiA"
    const val ABOUT_ACCOUNT_QUERY = "zs_jFPFT78rBpXv9Z3U2YQ"
    const val USER_TWEETS = "E3opETHurmVJflFsUBVuUQ"

    // Social Graph
    const val FOLLOWING = "zx6e-TLzRkeDO_a7p4b3JQ"
    const val FOLLOWERS = "IOh4aS6UdGWGJUYTqliQ7Q"

    // Engagement (Mutations)
    const val FAVORITE_TWEET = "lI07N6Otwv1PhnEgXILM7A"
    const val UNFAVORITE_TWEET = "ZYKSe-w7KEslx3JhSIk5LA"
    const val CREATE_RETWEET = "ojPdsZsimiJrUGLR1sjUtA"
    const val DELETE_RETWEET = "iQtK4dl5hBmXewYZuEOKVw"

    // Post (Mutations)
    const val CREATE_TWEET = "IID9x6WsdMnTlXnzXGq8ng"
    const val DELETE_TWEET = "VaenaVgh5q5ih7kvyVjgtg"

    // Follow (Mutations)
    const val CREATE_FRIENDSHIP = "OPwKc1HXnBT_bWXfAlo-9g"
    const val DESTROY_FRIENDSHIP = "ppXWuagMNXgvzx6WoXBW0Q"

    // Bookmarks
    const val BOOKMARKS = "VFdMm9iVZxlU6hD86gfW_A"
    const val CREATE_BOOKMARK = "aoDbu3RHznuiSkQ9aNM67Q"
    const val DELETE_BOOKMARK = "Wlmlj2-xzyS1GN3a6cj-mQ"
    const val BOOKMARK_FOLDER_TIMELINE = "KJIQpsvxrTfRIlbaRIySHQ"

    // Likes
    const val LIKES = "lIDpu_NWL7_VhimGGt0o6A"

    // Lists
    const val LIST_OWNERSHIPS = "wQcOSjSQ8NtgxIwvYl1lMg"
    const val LIST_MEMBERSHIPS = "BlEXXdARdSeL_0KyKHHvvg"
    const val LIST_LATEST_TWEETS_TIMELINE = "RlZzktZY_9wJynoepm8ZsA"

    // Explore
    const val GENERIC_TIMELINE_BY_ID = "FS17m3eEMcYdJvjPP1Upvw"
}
