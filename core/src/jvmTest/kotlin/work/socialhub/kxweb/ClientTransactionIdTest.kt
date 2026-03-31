package work.socialhub.kxweb

import work.socialhub.kxweb.internal.share.ClientTransactionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ClientTransactionIdTest {

    @Test
    fun testGenerateSimpleFallback() {
        // Without pair data, should generate simple random ID
        ClientTransactionId.clearCache()
        val id = ClientTransactionId.generate()
        assertEquals(20, id.length)
        assertTrue(id.all { it.isLetterOrDigit() })
    }

    @Test
    fun testGenerateSimpleFallbackUnique() {
        ClientTransactionId.clearCache()
        val ids = (1..100).map { ClientTransactionId.generate() }.toSet()
        assertTrue(ids.size > 90, "Simple fallback IDs should be mostly unique")
    }

    @Test
    fun testGenerateWithPairData() {
        // Set mock pair data
        val keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        ClientTransactionId.setPairData(keyBytes, "test-animation-key")

        val id = ClientTransactionId.generate("GET", "/i/api/graphql/abc/SearchTimeline")
        assertTrue(id.isNotEmpty())
        // Crypto-based ID should be different from simple
        // (it's base64 encoded so may contain +, /, =)

        ClientTransactionId.clearCache()
    }

    @Test
    fun testGenerateWithPairDataDifferentPaths() {
        val keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        ClientTransactionId.setPairData(keyBytes, "test-animation-key")

        val id1 = ClientTransactionId.generate("GET", "/path1")
        val id2 = ClientTransactionId.generate("GET", "/path2")

        // Different paths should produce different IDs
        assertNotEquals(id1, id2)

        ClientTransactionId.clearCache()
    }

    @Test
    fun testGenerateWithPairDataDifferentMethods() {
        val keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        ClientTransactionId.setPairData(keyBytes, "test-animation-key")

        val id1 = ClientTransactionId.generate("GET", "/path")
        val id2 = ClientTransactionId.generate("POST", "/path")

        assertNotEquals(id1, id2)

        ClientTransactionId.clearCache()
    }

    @Test
    fun testIsPairDataAvailable() {
        ClientTransactionId.clearCache()
        assertFalse(ClientTransactionId.isPairDataAvailable())

        ClientTransactionId.setPairData(byteArrayOf(0x01), "key")
        assertTrue(ClientTransactionId.isPairDataAvailable())

        ClientTransactionId.clearCache()
        assertFalse(ClientTransactionId.isPairDataAvailable())
    }
}
