# utf8parse-kotlin

Kotlin Multiplatform port of Rust [`utf8parse`](https://crates.io/crates/utf8parse) (`0.2.1`) providing a table-driven UTF-8 scalar parser across all 17 retained KotlinMania KMP targets and Swift Export.

## Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:utf8parse-kotlin:0.1.0")
}
```

## Kotlin Usage

```kotlin
import io.github.kotlinmania.utf8parse.CallbackReceiver
import io.github.kotlinmania.utf8parse.Parser

val parser = Parser.new()
val receiver = CallbackReceiver(
    onCodepoint = { ch -> println(ch) },
    onInvalidSequence = { println("invalid UTF-8") },
)
for (b in "héllo".encodeToByteArray()) {
    parser.advance(receiver, b.toUByte())
}
```

## Swift Usage

```swift
import Utf8parse
import RustLang

let parser = Parser.Companion.shared.new()
let receiver = CallbackReceiver(
    onCodepoint: codepointCallback { ch in print(ch.code) },
    onInvalidSequence: invalidSequenceCallback { print("invalid") }
)
```

## License

Dual-licensed under [Apache-2.0](tmp/utf8parse/LICENSE-APACHE) or [MIT](tmp/utf8parse/LICENSE-MIT), matching upstream `utf8parse`.
