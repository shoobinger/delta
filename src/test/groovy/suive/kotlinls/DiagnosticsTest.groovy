package suive.kotlinls

import org.junit.jupiter.api.Test

import java.nio.file.Files

class DiagnosticsTest extends LanguageServerTest {

    @Test
    void "should receive diagnostics notification"() {
        def workspaceRoot = createWorkspace("/test-projects/maven")
        def testClass = Files.createFile(
                workspaceRoot.resolve("src/main/kotlin/suive/kotlinls/testproject/TestClass.kt"))
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
                workspaceRoot.resolve("src/main/kotlin/suive/kotlinls/testproject/TestClass.kt"))
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
            Thread.sleep 50

            testEditor.sendNotification("textDocument/didChange",
                    [textDocument  : [uri: testClass.toUri().toString()],
                     contentChanges: [[range: [start: [line: 2, character: 12], end: [line: 2, character: 12]], text: "\n    invalidSymbol${i.toString().padLeft(3, '0')}"]]])
            Thread.sleep 50
        }

        def diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        def diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbol099"
        assert diagnostic.range.start.line == 3
        assert diagnostic.range.start.character == 4
        assert diagnostic.range.end.line == 3
        assert diagnostic.range.end.character == 17
    }
}
