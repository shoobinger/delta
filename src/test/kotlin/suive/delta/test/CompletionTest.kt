package suive.delta.test

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class CompletionTest : LanguageServerTest() {

    @Test
    fun `should complete members of classes`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            class A {
                val prop: String = "123"
                fun method(): String = ""
                fun methodWithParams(p: Int): String = p.toString()
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
            "position": { "line": 11, "character": 10 },
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
                        node("label").asString().contains("methodWithParams(p: Int)")
                        node("insertText").isEqualTo("methodWithParams(")
                    }
                }
                .anySatisfy {
                    assertJson(it) {
                        node("label").asString().contains("prop: String")
                        node("insertText").isEqualTo("prop")
                    }
                }
        }
    }

    @Test
    fun `should take into account visibility modifiers`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        Files.walk(Paths.get(".").toAbsolutePath()).forEach {
            println(it)
        }
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            private class A {
                private val prop: String = "123"
                private fun method() {}
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
                .noneSatisfy {
                    assertJson(it) {
                        node("label").asString().contains("prop")
                    }
                }
                .noneSatisfy {
                    assertJson(it) {
                        node("label").asString().contains("method")
                    }
                }
        }
    }
}
