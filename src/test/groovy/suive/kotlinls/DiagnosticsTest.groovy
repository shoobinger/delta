package suive.kotlinls

import org.junit.jupiter.api.Test

import java.nio.file.Files

class DiagnosticsTest extends LanguageServerTest {

    @Test
    void "should receive diagnostics notification"() {
        def workspaceRoot = createWorkspace()
        def testClass = Files.createFile(workspaceRoot.resolve("TestClass.kt"))
        testClass << """
            class TestClass {
                fun testMethod() {
                    invalidSymbol
                }
            }
        """.stripIndent().trim()

        testEditor.initialize(workspaceRoot)

        def diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
//        assert diagnosticNotification.params.uri == testClass.toAbsolutePath().toString()
        assert diagnosticNotification.params.diagnostics.size == 1
        def diagnostic = diagnosticNotification.params.diagnostics.first()
        assert diagnostic.message == "unresolved reference: invalidSymbol"
        assert diagnostic.range.start.line == 3
        assert diagnostic.range.start.character == 13
        assert diagnostic.range.end.line == 3
        assert diagnostic.range.end.character == 13
    }
}
