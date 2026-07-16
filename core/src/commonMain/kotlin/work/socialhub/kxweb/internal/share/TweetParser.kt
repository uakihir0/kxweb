package work.socialhub.kxweb.internal.share

import work.socialhub.kxweb.internal.entity.TweetResult
import work.socialhub.kxweb.internal.entity.TimelineInstruction
import work.socialhub.kxweb.internal.entity.UserResult
import work.socialhub.kxweb.model.Media
import work.socialhub.kxweb.model.Tweet
import work.socialhub.kxweb.model.User

object TweetParser {

    fun parseTweetResult(tweetResult: TweetResult): Tweet {
        val legacy = tweetResult.legacy

        val user = tweetResult.core?.userResults?.result?.let { parseUserResult(it) }

        val mediaEntities = legacy?.extendedEntities?.media
            ?: legacy?.entities?.media
            ?: emptyList()

        val mediaList = mediaEntities.map { entity ->
            Media(
                type = entity.type,
                url = entity.mediaUrlHttps,
                width = entity.originalInfo?.width,
                height = entity.originalInfo?.height,
            )
        }

        val viewCount = tweetResult.views?.count?.toLongOrNull()

        return Tweet(
            id = tweetResult.restId,
            text = legacy?.fullText,
            createdAt = legacy?.createdAt,
            user = user,
            replyCount = legacy?.replyCount,
            retweetCount = legacy?.retweetCount,
            favoriteCount = legacy?.favoriteCount,
            bookmarkCount = legacy?.bookmarkCount,
            quoteCount = legacy?.quoteCount,
            media = mediaList,
            viewCount = viewCount,
            inReplyToStatusId = legacy?.inReplyToStatusIdStr,
            conversationId = legacy?.conversationIdStr,
            lang = legacy?.lang,
        )
    }

    fun parseUserResult(userResult: UserResult): User {
        val userLegacy = userResult.legacy
        val userCore = userResult.core
        return User(
            id = userResult.restId,
            screenName = userCore?.screenName ?: userLegacy?.screenName,
            name = userCore?.name ?: userLegacy?.name,
            description = userLegacy?.description,
            profileImageUrl = userLegacy?.profileImageUrlHttps,
            profileBannerUrl = userLegacy?.profileBannerUrl,
            followersCount = userLegacy?.followersCount,
            followingCount = userLegacy?.friendsCount,
            statusesCount = userLegacy?.statusesCount,
            listedCount = userLegacy?.listedCount,
            verified = userResult.isBlueVerified ?: userLegacy?.verified,
            createdAt = userCore?.createdAt ?: userLegacy?.createdAt,
            location = userLegacy?.location,
            url = userLegacy?.url,
        )
    }

    data class TimelineParseResult(
        val tweets: List<Tweet>,
        val cursor: String? = null,
    )

    fun parseTimelineInstructions(instructions: List<TimelineInstruction>): TimelineParseResult {
        val tweets = mutableListOf<Tweet>()
        var cursor: String? = null

        for (instruction in instructions) {
            val entries = instruction.entries ?: continue

            for (entry in entries) {
                val content = entry.content ?: continue

                if (content.cursorType == "Bottom") {
                    cursor = content.value
                    continue
                }

                val tweetResult = content.itemContent
                    ?.tweetResults
                    ?.result
                    ?: continue

                val legacy = tweetResult.legacy ?: continue
                tweets.add(parseTweetResult(tweetResult))
            }
        }

        return TimelineParseResult(tweets, cursor)
    }

    data class UserTimelineParseResult(
        val users: List<User>,
        val cursor: String? = null,
    )

    fun parseUserTimelineInstructions(instructions: List<TimelineInstruction>): UserTimelineParseResult {
        val users = mutableListOf<User>()
        var cursor: String? = null

        for (instruction in instructions) {
            val entries = instruction.entries ?: continue

            for (entry in entries) {
                val content = entry.content ?: continue

                if (content.cursorType == "Bottom") {
                    cursor = content.value
                    continue
                }

                // Single-item entry (TimelineItem)
                content.itemContent?.userResults?.result?.let {
                    users.add(parseUserResult(it))
                }

                // Module entry (TimelineModule with grouped items), e.g. the
                // "People" tab of search results.
                content.items?.forEach { moduleItem ->
                    moduleItem.item?.itemContent?.userResults?.result?.let {
                        users.add(parseUserResult(it))
                    }
                }
            }
        }

        return UserTimelineParseResult(users, cursor)
    }
}
