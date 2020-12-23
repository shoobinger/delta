package suive.kotlinls

import org.junit.jupiter.api.Test

class CompletionTest extends LanguageServerTest {

    @Test
    void "should receive completion for java lang classes"() {
        def workspaceRoot = createWorkspace()
        testEditor.initialize(workspaceRoot)
        testEditor.write("TestClass.kt", """
            class TestClass {
                fun testMethod() {
                    
                }
            }
        """.stripIndent().trim())
        testEditor.moveCursor(3, 9) // To the method body.
        testEditor.write("TestClass.kt", "S")
        def response = testEditor.request("textDocument/completion", [
                textDocument: [uri: workspaceRoot.resolve("TestClass.kt").toString()],
                position    : [line: 2, character: 9]
        ])
        assert response.isIncomplete == false
        assert response.items.contains("String")
    }
}
