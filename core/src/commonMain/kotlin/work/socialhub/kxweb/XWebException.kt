package work.socialhub.kxweb

class XWebException : RuntimeException {
    var status: Int? = null
    var body: String? = null

    constructor(
        message: String?,
        exception: Exception?,
        status: Int? = null,
        body: String? = null,
    ) : super(message, exception) {
        this.status = status
        this.body = body
    }

    constructor(message: String) : super(message)
    constructor(exception: Exception?) : super(exception)
}
