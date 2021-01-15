package suive.delta.test.completion

import org.assertj.core.api.ListAssert
import suive.delta.test.LanguageServerTest
import java.nio.file.Path

open class CompletionTest : LanguageServerTest() {
    protected fun sendCompletionRequest(
        file: Path,
        line: Int,
        char: Int,
        partialResultToken: String? = null
    ) = testEditor.request(
        "textDocument/completion", """{
            "textDocument": { "uri": "${file.toUri()}" },
            "position": { "line": $line, "character": $char },
            "context": {
              "triggerKind": 2,
              "triggerCharacter": "."
            }${if (partialResultToken != null) ", \"partialResultToken\": \"$partialResultToken\"" else ""}
        }""".trimIndent()
    )

    protected fun assertCompletionResponse(response: Any?, labels: List<String>) {
        assertJson(response) {
            val items = node("result.items").isNotNull.isArray
            assertCompletionItems(items, labels)
        }
    }

    protected fun assertCompletionItems(items: ListAssert<Any>, labels: List<String>) {
        for (label in labels) {
            items.anySatisfy {
                assertJson(it) {
                    node("label").asString().contains(label)
                }
            }
        }
    }

    protected fun Path.assertCompletion(line: Int, char: Int, labels: List<String>) {
        val response = sendCompletionRequest(this, line, char)
        assertCompletionResponse(response, labels)
    }
}
