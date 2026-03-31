package work.socialhub.kxweb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XWebSessionTest {

    @Test
    fun testCookieSession() {
        val session = XWebSession.cookie("auth123", "csrf456")
        assertIs<XWebSession.Cookie>(session)
        assertEquals("auth123", session.authToken)
        assertEquals("csrf456", session.csrfToken)
        assertNull(session.label)
    }

    @Test
    fun testCookieSessionWithLabel() {
        val session = XWebSession.cookie("auth123", "csrf456", label = "user1")
        assertIs<XWebSession.Cookie>(session)
        assertEquals("user1", session.label)
    }

    @Test
    fun testOAuthSession() {
        val session = XWebSession.oauth("token1", "secret1")
        assertIs<XWebSession.OAuth>(session)
        assertEquals("token1", session.oauthToken)
        assertEquals("secret1", session.oauthSecret)
        assertNull(session.label)
    }

    @Test
    fun testOAuthSessionWithLabel() {
        val session = XWebSession.oauth("token1", "secret1", label = "bot1")
        assertIs<XWebSession.OAuth>(session)
        assertEquals("bot1", session.label)
    }

    @Test
    fun testParseJsonLinesOAuth() {
        val jsonl = """{"oauth_token":"tok1","oauth_token_secret":"sec1"}"""
        val sessions = XWebSession.parseJsonLines(jsonl)

        assertEquals(1, sessions.size)
        val session = sessions.first()
        assertIs<XWebSession.OAuth>(session)
        assertEquals("tok1", session.oauthToken)
        assertEquals("sec1", session.oauthSecret)
    }

    @Test
    fun testParseJsonLinesCookie() {
        val jsonl = """{"kind":"cookie","auth_token":"at1","ct0":"ct1","username":"user1"}"""
        val sessions = XWebSession.parseJsonLines(jsonl)

        assertEquals(1, sessions.size)
        val session = sessions.first()
        assertIs<XWebSession.Cookie>(session)
        assertEquals("at1", session.authToken)
        assertEquals("ct1", session.csrfToken)
        assertEquals("user1", session.label)
    }

    @Test
    fun testParseJsonLinesMultiple() {
        val jsonl = """
            {"oauth_token":"tok1","oauth_token_secret":"sec1"}
            {"kind":"cookie","auth_token":"at2","ct0":"ct2","username":"user2"}
            {"oauth_token":"tok3","oauth_token_secret":"sec3","username":"bot3"}
        """.trimIndent()
        val sessions = XWebSession.parseJsonLines(jsonl)

        assertEquals(3, sessions.size)
        assertIs<XWebSession.OAuth>(sessions[0])
        assertIs<XWebSession.Cookie>(sessions[1])
        assertIs<XWebSession.OAuth>(sessions[2])
    }

    @Test
    fun testParseJsonLinesSkipsInvalidLines() {
        val jsonl = """
            {"oauth_token":"tok1","oauth_token_secret":"sec1"}
            not valid json

            {"bad":"data"}
            {"auth_token":"at1","ct0":"ct1"}
        """.trimIndent()
        val sessions = XWebSession.parseJsonLines(jsonl)

        assertEquals(2, sessions.size)
        assertIs<XWebSession.OAuth>(sessions[0])
        assertIs<XWebSession.Cookie>(sessions[1])
    }

    @Test
    fun testParseJsonLinesEmpty() {
        val sessions = XWebSession.parseJsonLines("")
        assertTrue(sessions.isEmpty())
    }
}
