import Testing
import Utf8parse
import RustLang

@Suite("Utf8parse Swift Export Tests")
struct Utf8parseExportTests {
    @Test("Utf8parse parses ASCII, multibyte scalars, and invalid bytes across Swift boundary")
    func testParserRoundTrip() throws {
        let parser = Parser.Companion.shared.new()
        var codepoints: [UInt32] = []
        var invalidCount = 0

        let receiver = CallbackReceiver(
            onCodepoint: { ch in
                codepoints.append(ch.code)
            },
            onInvalidSequence: {
                invalidCount += 1
            }
        )

        // 'A' (0x41), invalid 0xFF, 'é' (C3 A9), '😀' U+1F600 (F0 9F 98 80)
        let bytes: [UInt8] = [0x41, 0xFF, 0xC3, 0xA9, 0xF0, 0x9F, 0x98, 0x80]
        for byte in bytes {
            parser.advance(receiver: receiver, byte: byte)
        }

        #expect(codepoints == [0x41, 0xE9, 0x1F600])
        #expect(invalidCount == 1)

        let fresh = Parser.Companion.shared.`default`()
        #expect(fresh == Parser())
    }
}
