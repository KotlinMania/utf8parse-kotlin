// port-lint: tests tests/utf-8-demo.rs
package io.github.kotlinmania.utf8parse

import io.github.kotlinmania.rustlang.core.char.RustChar
import kotlin.test.Test
import kotlin.test.assertEquals

private class StringWrapper : Receiver {
    val buffer: StringBuilder = StringBuilder()

    override fun codepoint(ch: RustChar) {
        buffer.append(ch.toString())
    }

    override fun invalidSequence() {}
}

public class Utf8DemoTest {
    @Test
    public fun utf8parseTest() {
        val utf8Demo = loadUtf8DemoBytes()
        val parser = Parser.new()

        // utf8parse implementation
        val actual = StringWrapper()
        for (byte in utf8Demo) {
            parser.advance(actual, byte.toUByte())
        }

        // standard library implementation
        val expected = utf8Demo.decodeToString()

        assertEquals(expected, actual.buffer.toString())
    }
}
