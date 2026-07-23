package work.socialhub.kxweb

import work.socialhub.kxweb.util.Sha256Util
import kotlin.test.Test
import kotlin.test.assertEquals

class Sha256UtilTest {

    @Test
    fun testEmptyInput() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb924" +
                "27ae41e4649b934ca495991b7852b855",
            Sha256Util.hash(byteArrayOf()).toHex(),
        )
    }

    @Test
    fun testAbc() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223" +
                "b00361a396177a9cb410ff61f20015ad",
            Sha256Util.hash("abc".encodeToByteArray()).toHex(),
        )
    }

    @Test
    fun testMultiBlockInput() {
        val input = "abcdbcdecdefdefgefghfghighijhijk" +
            "ijkljklmklmnlmnomnopnopq"

        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039" +
                "a33ce45964ff2167f6ecedd419db06c1",
            Sha256Util.hash(input.encodeToByteArray()).toHex(),
        )
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { byte ->
            (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
        }
    }
}
