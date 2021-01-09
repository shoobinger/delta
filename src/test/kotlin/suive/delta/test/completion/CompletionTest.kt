package suive.delta.test.completion

import suive.delta.test.LanguageServerTest
import java.nio.file.Path

open class CompletionTest : LanguageServerTest() {
    protected fun sendCompletionRequest(file: Path, line: Int, char: Int) = testEditor.request(
        "textDocument/completion", """{
            "textDocument": { "uri": "${file.toUri()}" },
            "position": { "line": $line, "character": $char },
            "context": {
              "triggerKind": 2,
              "triggerCharacter": "."
            }
        }""".trimIndent()
    )
}
