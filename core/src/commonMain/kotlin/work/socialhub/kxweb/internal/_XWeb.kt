package work.socialhub.kxweb.internal

import work.socialhub.kxweb.XWeb
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.SearchResource
import work.socialhub.kxweb.api.TweetResource

class _XWeb(
    config: XWebConfig
) : XWeb {

    private val search: SearchResource = _SearchResource(config)
    private val tweet: TweetResource = _TweetResource(config)

    override fun search() = search
    override fun tweet() = tweet
}
