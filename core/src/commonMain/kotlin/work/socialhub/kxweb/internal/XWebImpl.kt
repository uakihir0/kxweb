package work.socialhub.kxweb.internal

import work.socialhub.kxweb.XWeb
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.SearchResource
import work.socialhub.kxweb.api.TweetResource

class XWebImpl(
    config: XWebConfig
) : XWeb {

    private val search: SearchResource = SearchResourceImpl(config)
    private val tweet: TweetResource = TweetResourceImpl(config)

    override fun search() = search
    override fun tweet() = tweet
}
