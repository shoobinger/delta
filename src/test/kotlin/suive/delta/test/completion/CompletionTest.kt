package suive.delta.test.completion

import suive.delta.Json
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

    protected fun assertCompletionItems(response: Json?, labels: List<String>) {
        assertJson(response) {
            val items = node("result.items").isNotNull.isArray.hasSizeGreaterThan(1)

            for (label in labels) {
                items.anySatisfy {
                    assertJson(it) {
                        node("label").asString().contains(label)
                    }
                }
            }
        }
    }

    protected fun Path.assertCompletion(line: Int, char: Int, labels: List<String>) {
        val response = sendCompletionRequest(this, line, char)
        assertCompletionItems(response, labels)
    }
}
