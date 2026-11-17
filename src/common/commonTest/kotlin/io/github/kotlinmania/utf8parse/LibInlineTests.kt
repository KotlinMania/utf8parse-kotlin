package io.github.kotlinmania.utf8parse

import io.github.kotlinmania.rustlang.core.char.RustChar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

// Unported: upstream `#[cfg(all(feature = "nightly", test))] mod benches` uses unstable nightly
// `extern crate test` / `Bencher`, which has no commonTest equivalent.

private class RecordingReceiver : Receiver {
    val chars: MutableList<RustChar> = mutableListOf()
    var invalidCount: Int = 0

    override fun codepoint(ch: RustChar) {
        chars.add(ch)
    }

    override fun invalidSequence() {
        invalidCount += 1
    }
}

public class LibInlineTests {
    @Test
    public fun parsesFourByteScalarAndInvalidSequences() {
        val parser = Parser.new()
        val receiver = RecordingReceiver()

        // 'A' (0x41), invalid continuation 0x80, '😀' U+1F600 (F0 9F 98 80), overlong C0 80, surrogate ED A0 80
        val bytes =
            ubyteArrayOf(
                0x41u,
                0x80u,
                0xF0u,
                0x9Fu,
                0x98u,
                0x80u,
                0xC0u,
                0x80u,
                0xEDu,
                0xA0u,
                0x80u,
            )
        for (b in bytes) {
            parser.advance(receiver, b)
        }

        assertEquals(listOf(RustChar(0x41u), RustChar(0x1F600u)), receiver.chars)
        // 0x80 (1), C0 (1), 0x80 (1), ED->A0 (1 at A0), 0x80 (1) => 5 invalid events
        assertEquals(5, receiver.invalidCount)
    }

    @Test
    public fun parserCloneAndEqualityMatchState() {
        val first = Parser.new()
        val cloneAtGround = first.copy()
        assertEquals(first, cloneAtGround)
        assertEquals(first.hashCode(), cloneAtGround.hashCode())

        val receiver = RecordingReceiver()
        // Feed leading byte of a 2-byte sequence (C3 A9 = 'é') without completing it
        first.advance(receiver, 0xC3u)
        assertNotEquals(first, cloneAtGround)

        val cloneMidSequence = first.copy()
        assertEquals(first, cloneMidSequence)
        first.advance(receiver, 0xA9u)
        assertEquals(listOf(RustChar(0xE9u)), receiver.chars)
        assertEquals(cloneAtGround, first)
    }
}
