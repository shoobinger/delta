package suive.delta

import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class DiagnosticsTest extends LanguageServerTest {

    @Test
    void "should receive diagnostics notification"() {
        def workspaceRoot = createWorkspace("/test-projects/maven")
        def testClass = Files.createFile(
                workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt"))
        testClass << """
            class TestClass {
                fun testMethod() {
                    invalidSymbol
                }
            }
        """.stripIndent(true).trim()

        testEditor.initialize(workspaceRoot)

        def diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        def diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbol"
        assert diagnostic.range.start.line == 2
        assert diagnostic.range.start.character == 8
        assert diagnostic.range.end.line == 2
        assert diagnostic.range.end.character == 21

        // Add 's' to the end.
        testEditor.sendNotification("textDocument/didChange",
                [textDocument  : [uri: testClass.toUri().toString()],
                 contentChanges: [[range: [start: [line: 2, character: 21], end: [line: 2, character: 21]], text: "s"]]])

        diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbols"
        assert diagnostic.range.start.line == 2
        assert diagnostic.range.start.character == 8
        assert diagnostic.range.end.line == 2
        assert diagnostic.range.end.character == 22
    }

    @Test
    void "fast typing test"() {
        def workspaceRoot = createWorkspace("/test-projects/maven")
        def testClass = Files.createFile(
                workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt"))
        testClass << """
            package suive
            
            fun main() {
                invalidSymbol000
            }
        """.stripIndent(true).trim()

        testEditor.initialize(workspaceRoot)

        for (i in 0..100) {
            testEditor.sendNotification("textDocument/didChange",
                    [textDocument  : [uri: testClass.toUri().toString()],
                     contentChanges: [[range: [start: [line: 2, character: 12], end: [line: 3, character: 20]], text: ""]]])
            Thread.sleep 10

            testEditor.sendNotification("textDocument/didChange",
                    [textDocument  : [uri: testClass.toUri().toString()],
                     contentChanges: [[range: [start: [line: 2, character: 12], end: [line: 2, character: 12]], text: "\n    invalidSymbol${i.toString().padLeft(3, '0')}"]]])
            Thread.sleep 10
        }

        def diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        def diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbol100"
        assert diagnostic.range.start.line == 3
        assert diagnostic.range.start.character == 4
        assert diagnostic.range.end.line == 3
        assert diagnostic.range.end.character == 20
    }

    @Test
    void "diagnostics are cleared after removing issue"() {
        def workspaceRoot = createWorkspace("/test-projects/maven")
        def testClass = Files.createFile(
                workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt"))
        testClass << """
            class TestClass {
                fun testMethod() {
                    invalidSymbol
                }
            }
        """.stripIndent(true).trim()

        testEditor.initialize(workspaceRoot)

        def diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        def diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbol"
        assert diagnostic.range.start.line == 2
        assert diagnostic.range.start.character == 8
        assert diagnostic.range.end.line == 2
        assert diagnostic.range.end.character == 21

        // Remove invalidSymbol.
        testEditor.sendNotification "textDocument/didChange",
                [textDocument  : [uri: testClass.toUri().toString()],
                 contentChanges: [[range: [start: [line: 2, character: 8], end: [line: 2, character: 21]], text: ""]]]

        // Empty diagnostics are received.
        diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 0

        // Should not receive empty diagnostics.
        // Remove method.
        testEditor.sendNotification "textDocument/didChange",
                [textDocument  : [uri: testClass.toUri().toString()],
                 contentChanges: [[range: [start: [line: 1, character: 0], end: [line: 3, character: 5]], text: ""]]]
        try {
            def notification = CompletableFuture.supplyAsync {
                testEditor.getNotification("textDocument/publishDiagnostics")
            }.get(1, TimeUnit.SECONDS)
            assert notification == null
        } catch (TimeoutException ignored) {
            // Test passed
        }
    }
}
