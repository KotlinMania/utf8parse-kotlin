#if canImport(Testing)
import Testing
import Utf8parse

@Suite("Utf8parse Swift Export Tests")
struct Utf8parseExportTests {
    @Test("Utf8parse swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Utf8parse swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import Utf8parse

final class Utf8parseExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Utf8parse swift module imported cleanly")
    }
}
#endif
