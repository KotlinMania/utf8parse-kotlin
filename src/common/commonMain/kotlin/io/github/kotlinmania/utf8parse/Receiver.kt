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
 * Callback-backed [Receiver] adapter delegating codepoint and invalid-sequence events.
 */
public class CallbackReceiver(
    private val onCodepoint: (RustChar) -> Unit,
    private val onInvalidSequence: () -> Unit,
) : Receiver {
    override fun codepoint(ch: RustChar) {
        onCodepoint(ch)
    }

    override fun invalidSequence() {
        onInvalidSequence()
    }
}
