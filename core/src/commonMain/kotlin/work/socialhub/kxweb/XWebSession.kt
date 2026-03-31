package work.socialhub.kxweb

import kotlin.js.JsExport

/**
 * Represents a single authenticated session for X (Twitter) API.
 * Used with [XWebSessionPool] for managing multiple sessions.
 *
 * Reference: Nitter's session management (auth.nim)
 */
@JsExport
sealed class XWebSession {

    /**
     * Optional label for identifying this session (e.g., username).
     */
    abstract val label: String?

    /**
     * Cookie-based session using auth_token and ct0 (CSRF token).
     * Endpoint: https://x.com/i/api/graphql
     */
    data class Cookie(
        val authToken: String,
        val csrfToken: String,
        val cookieString: String? = null,
        override val label: String? = null,
    ) : XWebSession()

    /**
     * OAuth1-based session using access token and secret.
     * Endpoint: https://api.x.com/graphql
     */
    data class OAuth(
        val oauthToken: String,
        val oauthSecret: String,
        override val label: String? = null,
    ) : XWebSession()

    companion object {
        /**
         * Create a cookie-based session.
         */
        fun cookie(
            authToken: String,
            csrfToken: String,
            cookieString: String? = null,
            label: String? = null,
        ): XWebSession = Cookie(authToken, csrfToken, cookieString, label)

        /**
         * Create an OAuth1-based session.
         */
        fun oauth(
            oauthToken: String,
            oauthSecret: String,
            label: String? = null,
        ): XWebSession = OAuth(oauthToken, oauthSecret, label)

        /**
         * Parse sessions from a JSONL string (one JSON object per line).
         * Compatible with Nitter's sessions.jsonl format.
         *
         * Cookie format: {"kind":"cookie","auth_token":"...","ct0":"...","username":"..."}
         * OAuth format:  {"oauth_token":"...","oauth_token_secret":"..."}
         */
        fun parseJsonLines(jsonLines: String): List<XWebSession> {
            return jsonLines.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && it.startsWith("{") }
                .mapNotNull { line -> parseJsonLine(line) }
        }

        private fun parseJsonLine(line: String): XWebSession? {
            return try {
                val fields = parseSimpleJson(line)
                when {
                    fields.containsKey("oauth_token") -> OAuth(
                        oauthToken = fields["oauth_token"] ?: return null,
                        oauthSecret = fields["oauth_token_secret"] ?: return null,
                        label = fields["username"],
                    )
                    fields.containsKey("auth_token") -> Cookie(
                        authToken = fields["auth_token"] ?: return null,
                        csrfToken = fields["ct0"] ?: return null,
                        label = fields["username"],
                    )
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }

        /**
         * Simple JSON string field parser (avoids kotlinx-serialization dependency
         * for this utility since sessions may come from external tools).
         */
        private fun parseSimpleJson(json: String): Map<String, String> {
            val result = mutableMapOf<String, String>()
            val content = json.trim().removeSurrounding("{", "}")
            var i = 0
            while (i < content.length) {
                // Find key
                val keyStart = content.indexOf('"', i)
                if (keyStart == -1) break
                val keyEnd = content.indexOf('"', keyStart + 1)
                if (keyEnd == -1) break
                val key = content.substring(keyStart + 1, keyEnd)

                // Find colon
                val colon = content.indexOf(':', keyEnd + 1)
                if (colon == -1) break

                // Find value
                val valueStart = content.indexOf('"', colon + 1)
                if (valueStart == -1) {
                    i = colon + 1
                    continue
                }
                val valueEnd = content.indexOf('"', valueStart + 1)
                if (valueEnd == -1) break
                val value = content.substring(valueStart + 1, valueEnd)

                result[key] = value
                i = valueEnd + 1
            }
            return result
        }
    }
}
