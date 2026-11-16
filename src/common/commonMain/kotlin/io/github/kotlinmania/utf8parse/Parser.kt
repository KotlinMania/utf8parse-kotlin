package io.github.kotlinmania.utf8parse

import io.github.kotlinmania.rustlang.core.char.RustChar
import io.github.kotlinmania.utf8parse.types.Action
import io.github.kotlinmania.utf8parse.types.State

/**
 * Continuation bytes are masked with this value.
 */
private const val CONTINUATION_MASK: UByte = 0x3Fu

/**
 * A parser for UTF-8 characters.
 *
 * Repeatedly call [advance] with bytes to emit UTF-8 characters.
 *
 * This implements a table-driven UTF-8 state machine whose only branch is on
 * the internal transition action.
 */
public class Parser private constructor(
    private var point: UInt,
    private var state: State,
) {
    /**
     * Creates a new parser in the default ground state.
     */
    public constructor() : this(point = 0u, state = State.Ground)

    /**
     * Advance the parser.
     *
     * The provided [receiver] will be called whenever a codepoint is completed
     * or an invalid sequence is detected.
     */
    public fun advance(
        receiver: Receiver,
        byte: UByte,
    ) {
        val step = state.advance(byte)
        performAction(receiver, byte, step.action)
        state = step.state
    }

    private fun performAction(
        receiver: Receiver,
        byte: UByte,
        action: Action,
    ) {
        when (action) {
            Action.InvalidSequence -> {
                point = 0u
                receiver.invalidSequence()
            }
            Action.EmitByte -> {
                receiver.codepoint(RustChar(byte.toUInt()))
            }
            Action.SetByte1 -> {
                val completed = point or (byte and CONTINUATION_MASK).toUInt()
                val c = RustChar.fromU32Unchecked(completed)
                point = 0u
                receiver.codepoint(c)
            }
            Action.SetByte2 -> {
                point = point or ((byte and CONTINUATION_MASK).toUInt() shl 6)
            }
            Action.SetByte2Top -> {
                point = point or ((byte and 0x1Fu.toUByte()).toUInt() shl 6)
            }
            Action.SetByte3 -> {
                point = point or ((byte and CONTINUATION_MASK).toUInt() shl 12)
            }
            Action.SetByte3Top -> {
                point = point or ((byte and 0x0Fu.toUByte()).toUInt() shl 12)
            }
            Action.SetByte4 -> {
                point = point or ((byte and 0x07u.toUByte()).toUInt() shl 18)
            }
        }
    }

    /**
     * Returns an independent copy preserving the parser's current point and state.
     */
    public fun copy(): Parser = Parser(point = point, state = state)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Parser) return false
        return point == other.point && state == other.state
    }

    override fun hashCode(): Int = 31 * point.hashCode() + state.hashCode()

    override fun toString(): String = "Parser(point=$point, state=$state)"

    public companion object {
        /**
         * Create a new [Parser].
         */
        public fun new(): Parser = Parser(point = 0u, state = State.Ground)

        /**
         * Create a default [Parser].
         */
        public fun default(): Parser = Parser()
    }
}
