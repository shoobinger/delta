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
        """.stripIndent().trim()

        testEditor.initialize(workspaceRoot)

        def diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        def diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbol"
        assert diagnostic.range.start.line == 2
        assert diagnostic.range.start.character == 12
        assert diagnostic.range.end.line == 2
        assert diagnostic.range.end.character == 25

        // Add 's' to the end.
        testEditor.sendNotification("textDocument/didChange",
                [textDocument  : [uri: testClass.toUri().toString()],
                 contentChanges: [[range: [start: [line: 2, character: 25], end: [line: 2, character: 25]], text: "s"]]])

        diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assert diagnosticNotification.params.uri == testClass.toUri().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "Unresolved reference: invalidSymbols"
        assert diagnostic.range.start.line == 2
        assert diagnostic.range.start.character == 12
        assert diagnostic.range.end.line == 2
        assert diagnostic.range.end.character == 26

    }
}
