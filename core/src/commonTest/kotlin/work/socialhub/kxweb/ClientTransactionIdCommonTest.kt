package work.socialhub.kxweb

import work.socialhub.kxweb.internal.share.ClientTransactionId
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientTransactionIdCommonTest {

    @Test
    fun testExtractAnimationIndices() {
        val bundle = """(value[7], 16);(other[12], 16)"""

        assertEquals(
            listOf(7, 12),
            ClientTransactionId.extractAnimationIndices(bundle),
        )
    }
}
