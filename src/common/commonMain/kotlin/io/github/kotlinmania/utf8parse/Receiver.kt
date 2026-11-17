package io.github.kotlinmania.utf8parse

import io.github.kotlinmania.rustlang.core.char.RustChar

/**
 * Handles codepoint and invalid sequence events from the parser.
 */
public interface Receiver {
    /**
     * Called whenever a codepoint is parsed successfully.
     */
    public fun codepoint(ch: RustChar)

    /**
     * Called when an invalid sequence is detected.
     */
    public fun invalidSequence()
}

/**
 * Functional callback invoked when a Unicode scalar codepoint is decoded.
 */
public fun interface CodepointCallback {
    /**
     * Receives a decoded [RustChar].
     */
    public fun onCodepoint(ch: RustChar)
}

/**
 * Functional callback invoked when an invalid UTF-8 byte sequence is encountered.
 */
public fun interface InvalidSequenceCallback {
    /**
     * Handles an invalid UTF-8 sequence event.
     */
    public fun onInvalidSequence()
}

/**
 * Callback-backed [Receiver] adapter delegating codepoint and invalid-sequence events.
 */
public class CallbackReceiver(
    private val onCodepoint: CodepointCallback,
    private val onInvalidSequence: InvalidSequenceCallback,
) : Receiver {
    override fun codepoint(ch: RustChar) {
        onCodepoint.onCodepoint(ch)
    }

    override fun invalidSequence() {
        onInvalidSequence.onInvalidSequence()
    }
}
