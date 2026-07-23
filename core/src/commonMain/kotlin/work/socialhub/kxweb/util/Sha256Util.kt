package work.socialhub.kxweb.util

/**
 * Platform-independent SHA-256 hash utility.
 */
object Sha256Util {

    private val initialHash = intArrayOf(
        1779033703, -1150833019, 1013904242, -1521486534,
        1359893119, -1694144372, 528734635, 1541459225,
    )

    private val roundConstants = intArrayOf(
        1116352408, 1899447441, -1245643825, -373957723,
        961987163, 1508970993, -1841331548, -1424204075,
        -670586216, 310598401, 607225278, 1426881987,
        1925078388, -2132889090, -1680079193, -1046744716,
        -459576895, -272742522, 264347078, 604807628,
        770255983, 1249150122, 1555081692, 1996064986,
        -1740746414, -1473132947, -1341970488, -1084653625,
        -958395405, -710438585, 113926993, 338241895,
        666307205, 773529912, 1294757372, 1396182291,
        1695183700, 1986661051, -2117940946, -1838011259,
        -1564481375, -1474664885, -1035236496, -949202525,
        -778901479, -694614492, -200395387, 275423344,
        430227734, 506948616, 659060556, 883997877,
        958139571, 1322822218, 1537002063, 1747873779,
        1955562222, 2024104815, -2067236844, -1933114872,
        -1866530822, -1538233109, -1090935817, -965641998,
    )

    fun hash(input: ByteArray): ByteArray {
        val bitLength = input.size.toLong() * 8
        val paddedLength = ((input.size + 9 + 63) / 64) * 64
        val message = ByteArray(paddedLength)
        input.copyInto(message)
        message[input.size] = 0x80.toByte()
        repeat(8) { index ->
            message[paddedLength - 1 - index] = (bitLength ushr (index * 8)).toByte()
        }

        val hash = initialHash.copyOf()
        val words = IntArray(64)
        for (offset in message.indices step 64) {
            for (index in 0 until 16) {
                val wordOffset = offset + index * 4
                words[index] =
                    ((message[wordOffset].toInt() and 0xFF) shl 24) or
                        ((message[wordOffset + 1].toInt() and 0xFF) shl 16) or
                        ((message[wordOffset + 2].toInt() and 0xFF) shl 8) or
                        (message[wordOffset + 3].toInt() and 0xFF)
            }
            for (index in 16 until 64) {
                val s0 = rotateRight(words[index - 15], 7) xor
                    rotateRight(words[index - 15], 18) xor
                    (words[index - 15] ushr 3)
                val s1 = rotateRight(words[index - 2], 17) xor
                    rotateRight(words[index - 2], 19) xor
                    (words[index - 2] ushr 10)
                words[index] = words[index - 16] + s0 + words[index - 7] + s1
            }

            var a = hash[0]
            var b = hash[1]
            var c = hash[2]
            var d = hash[3]
            var e = hash[4]
            var f = hash[5]
            var g = hash[6]
            var h = hash[7]

            for (index in 0 until 64) {
                val sum1 = rotateRight(e, 6) xor rotateRight(e, 11) xor rotateRight(e, 25)
                val choice = (e and f) xor (e.inv() and g)
                val temp1 = h + sum1 + choice + roundConstants[index] + words[index]
                val sum0 = rotateRight(a, 2) xor rotateRight(a, 13) xor rotateRight(a, 22)
                val majority = (a and b) xor (a and c) xor (b and c)
                val temp2 = sum0 + majority

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            hash[0] += a
            hash[1] += b
            hash[2] += c
            hash[3] += d
            hash[4] += e
            hash[5] += f
            hash[6] += g
            hash[7] += h
        }

        return ByteArray(32) { index ->
            val word = hash[index / 4]
            (word ushr (24 - (index % 4) * 8)).toByte()
        }
    }

    private fun rotateRight(value: Int, bits: Int): Int {
        return (value ushr bits) or (value shl (32 - bits))
    }
}
