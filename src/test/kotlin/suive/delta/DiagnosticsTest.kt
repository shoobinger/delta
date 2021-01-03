package suive.delta

import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import suive.delta.TestEditor.Companion.NEWLINE
import java.nio.file.Files

class DiagnosticsTest : LanguageServerTest() {

    @Test
    fun `should receive diagnostics notification`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            class TestClass {
                fun testMethod() {
                    invalidSymbol
                }
            }
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        val diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(diagnosticNotification)
        assertThatJson(diagnosticNotification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSize(1)
            with(node("params.diagnostics[0]")) {
                node("message").asString().isEqualTo("Unresolved reference: invalidSymbol")
                node("range.start.line").isEqualTo(2)
                node("range.start.character").isEqualTo(8)
                node("range.end.line").isEqualTo(2)
                node("range.end.character").isEqualTo(21)
            }
        }

        // Add 's' to the end.
        testEditor.sendNotification(
            "textDocument/didChange",
            """
                    {
                      "textDocument": { "uri": "${testClass.toUri()}" },
                      "contentChanges": [ 
                        { "range": { "start": { "line": 2, "character": 21 }, "end": { "line": 2, "character": 21 } },
                          "text": "s" }
                      ]
                    }
                """.trimIndent()
        )

        val notification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(notification)
        assertThatJson(notification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSize(1)
            with(node("params.diagnostics[0]")) {
                node("message").asString().isEqualTo("Unresolved reference: invalidSymbols")
                node("range.start.line").isEqualTo(2)
                node("range.start.character").isEqualTo(8)
                node("range.end.line").isEqualTo(2)
                node("range.end.character").isEqualTo(22)
            }
        }
    }

    @Test
    fun `fast typing test`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive
            
            fun main() {
                invalidSymbol000
            }
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        for (i in 0..100) {
            // Remove invalid symbol.
            testEditor.sendNotification(
                "textDocument/didChange",
                """
                    {
                      "textDocument": { "uri": "${testClass.toUri()}" },
                      "contentChanges": [ 
                        { "range": { "start": { "line": 2, "character": 12 }, "end": { "line": 3, "character": 20 } },
                          "text": "" }
                      ]
                    }
                """.trimIndent()
            )
            Thread.sleep(10)

            // Add invalid symbol.
            testEditor.sendNotification(
                "textDocument/didChange",
                """
                    {
                      "textDocument": { "uri": "${testClass.toUri()}" },
                      "contentChanges": [ 
                        { "range": { "start": { "line": 2, "character": 12 }, "end": { "line": 2, "character": 12 } },
                          "text": "\n    invalidSymbol${i.toString().padStart(3, '0')}" }
                      ]
                    }
                """.trimIndent()
            )
            Thread.sleep(10)
        }

        val notification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(notification)
        assertThatJson(notification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSize(1)
            with(node("params.diagnostics[0]")) {
                node("message").asString().isEqualTo("Unresolved reference: invalidSymbol100")
                node("range.start.line").isEqualTo(3)
                node("range.start.character").isEqualTo(4)
                node("range.end.line").isEqualTo(3)
                node("range.end.character").isEqualTo(20)
            }
        }
    }

    @Test
    fun `diagnostics are cleared after removing issue`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            class TestClass {
                fun testMethod() {
                    invalidSymbol
                }
            }
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        val notification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(notification)
        assertThatJson(notification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSize(1)
            with(node("params.diagnostics[0]")) {
                node("message").asString().isEqualTo("Unresolved reference: invalidSymbol")
                node("range.start.line").isEqualTo(2)
                node("range.start.character").isEqualTo(8)
                node("range.end.line").isEqualTo(2)
                node("range.end.character").isEqualTo(21)
            }
        }

        // Remove invalidSymbol.
        testEditor.sendNotification(
            "textDocument/didChange",
            """
                    {
                      "textDocument": { "uri": "${testClass.toUri()}" },
                      "contentChanges": [ 
                        { "range": { "start": { "line": 2, "character": 8 }, "end": { "line": 2, "character": 21 } },
                          "text": "" }
                      ]
                    }
                """.trimIndent()
        )

        // Empty diagnostics are received.
        val secondNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(secondNotification)
        assertThatJson(secondNotification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSize(0)
        }

        // Should not receive empty diagnostics.
        // Remove method (still a successful build).
        testEditor.sendNotification(
            "textDocument/didChange",
            """
                    {
                      "textDocument": { "uri": "${testClass.toUri()}" },
                      "contentChanges": [ 
                        { "range": { "start": { "line": 1, "character": 0 }, "end": { "line": 3, "character": 5 } },
                          "text": "" }
                      ]
                    }
                """.trimIndent()
        )

        assertThat(testEditor.getNotification("textDocument/publishDiagnostics", 2)).isNull()
    }
}
