import Testing
import Utf8parse

private final class ParseRecorder {
    var codepoints: [UInt32] = []
    var invalidCount: Int = 0
}

@Suite("Utf8parse Swift Export Tests")
struct Utf8parseExportTests {
    @Test("Utf8parse parses ASCII, multibyte scalars, and invalid bytes across Swift boundary")
    func testParserRoundTrip() throws {
        let parser = Parser.Companion.shared.new()
        let recorder = ParseRecorder()

        let receiver = CallbackReceiver(
            onCodepoint: codepointCallback { ch in
                recorder.codepoints.append(ch.code)
            },
            onInvalidSequence: invalidSequenceCallback {
                recorder.invalidCount += 1
            }
        )

        // 'A' (0x41), invalid 0xFF, 'é' (C3 A9), '😀' U+1F600 (F0 9F 98 80)
        let bytes: [UInt8] = [0x41, 0xFF, 0xC3, 0xA9, 0xF0, 0x9F, 0x98, 0x80]
        for byte in bytes {
            parser.advance(receiver: receiver, byte: byte)
        }

        #expect(recorder.codepoints == [0x41, 0xE9, 0x1F600])
        #expect(recorder.invalidCount == 1)

        let fresh = Parser.Companion.shared.`default`()
        #expect(fresh == Parser())
    }
}
