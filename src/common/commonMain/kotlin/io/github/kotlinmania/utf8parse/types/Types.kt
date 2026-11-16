// port-lint: source types.rs
package io.github.kotlinmania.utf8parse.types

// Types supporting the UTF-8 parser

/**
 * Action to take when receiving a byte.
 */
internal sealed class Action(
    val value: Int,
) {
    /**
     * Unexpected byte; sequence is invalid.
     */
    data object InvalidSequence : Action(0)

    /**
     * Received valid 7-bit ASCII byte which can be directly emitted.
     */
    data object EmitByte : Action(1)

    /**
     * Set the bottom continuation byte.
     */
    data object SetByte1 : Action(2)

    /**
     * Set the 2nd-from-last continuation byte.
     */
    data object SetByte2 : Action(3)

    /**
     * Set the 2nd-from-last byte which is part of a two byte sequence.
     */
    data object SetByte2Top : Action(4)

    /**
     * Set the 3rd-from-last continuation byte.
     */
    data object SetByte3 : Action(5)

    /**
     * Set the 3rd-from-last byte which is part of a three byte sequence.
     */
    data object SetByte3Top : Action(6)

    /**
     * Set the top byte of a four byte sequence.
     */
    data object SetByte4 : Action(7)
}

internal data class AdvanceStep(
    val state: State,
    val action: Action,
)

/**
 * States the parser can be in.
 *
 * There is a state for each initial input of the 3 and 4 byte sequences since
 * the following bytes are subject to different conditions than a tail byte.
 */
internal sealed class State(
    val value: Int,
) {
    /**
     * Ground state; expect anything.
     */
    data object Ground : State(0)

    /**
     * 3 tail bytes.
     */
    data object Tail3 : State(1)

    /**
     * 2 tail bytes.
     */
    data object Tail2 : State(2)

    /**
     * 1 tail byte.
     */
    data object Tail1 : State(3)

    /**
     * UTF8-3 starting with E0.
     */
    data object U32E0 : State(4)

    /**
     * UTF8-3 starting with ED.
     */
    data object U32Ed : State(5)

    /**
     * UTF8-4 starting with F0.
     */
    data object Utf843F0 : State(6)

    /**
     * UTF8-4 starting with F4.
     */
    data object Utf843F4 : State(7)

    /**
     * Advance the parser state.
     *
     * This takes the current state and input byte into consideration to determine
     * the next state and any action that should be taken.
     */
    fun advance(byte: UByte): AdvanceStep {
        val b = byte.toInt()
        return when (this) {
            State.Ground ->
                when (b) {
                    in 0x00..0x7F -> AdvanceStep(State.Ground, Action.EmitByte)
                    in 0xC2..0xDF -> AdvanceStep(State.Tail1, Action.SetByte2Top)
                    0xE0 -> AdvanceStep(State.U32E0, Action.SetByte3Top)
                    in 0xE1..0xEC -> AdvanceStep(State.Tail2, Action.SetByte3Top)
                    0xED -> AdvanceStep(State.U32Ed, Action.SetByte3Top)
                    in 0xEE..0xEF -> AdvanceStep(State.Tail2, Action.SetByte3Top)
                    0xF0 -> AdvanceStep(State.Utf843F0, Action.SetByte4)
                    in 0xF1..0xF3 -> AdvanceStep(State.Tail3, Action.SetByte4)
                    0xF4 -> AdvanceStep(State.Utf843F4, Action.SetByte4)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.U32E0 ->
                when (b) {
                    in 0xA0..0xBF -> AdvanceStep(State.Tail1, Action.SetByte2)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.U32Ed ->
                when (b) {
                    in 0x80..0x9F -> AdvanceStep(State.Tail1, Action.SetByte2)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.Utf843F0 ->
                when (b) {
                    in 0x90..0xBF -> AdvanceStep(State.Tail2, Action.SetByte3)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.Utf843F4 ->
                when (b) {
                    in 0x80..0x8F -> AdvanceStep(State.Tail2, Action.SetByte3)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.Tail3 ->
                when (b) {
                    in 0x80..0xBF -> AdvanceStep(State.Tail2, Action.SetByte3)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.Tail2 ->
                when (b) {
                    in 0x80..0xBF -> AdvanceStep(State.Tail1, Action.SetByte2)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
            State.Tail1 ->
                when (b) {
                    in 0x80..0xBF -> AdvanceStep(State.Ground, Action.SetByte1)
                    else -> AdvanceStep(State.Ground, Action.InvalidSequence)
                }
        }
    }

    companion object {
        fun default(): State = State.Ground
    }
}
