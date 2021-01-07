package suive.delta.test

import org.junit.jupiter.api.Test
import java.nio.file.Files

class CompletionTest : LanguageServerTest() {

    @Test
    fun `should complete instance methods of local classes`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            class A {
                fun method(): String = ""
                fun methodWithArgs(p: Int): String = p.toString()
            }
            
            class TestClass {
                fun testMethod() {
                    val a = A()
                    a.
                }
            }
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        // Completion request sent by the editor right after typing ".".
        val response = testEditor.request(
            "textDocument/completion", """{
            "textDocument": { "uri": "${testClass.toUri()}" },
            "position": { "line": 10, "character": 10 },
            "context": {
              "triggerKind": 2,
              "triggerCharacter": "."
            }
        }""".trimIndent()
        )

        assertJson(response) {
            node("result.items").isNotNull
            node("result.items").isArray.hasSizeGreaterThan(1)
                .anySatisfy {
                    assertJson(it) {
                        node("label").asString().contains("method()")
                        node("insertText").isEqualTo("method()")
                    }
                }
                .anySatisfy {
                    assertJson(it) {
                        node("label").asString().contains("methodWithArgs(p: Int)")
                        node("insertText").isEqualTo("methodWithArgs(")
                    }
                }
        }
    }
}
