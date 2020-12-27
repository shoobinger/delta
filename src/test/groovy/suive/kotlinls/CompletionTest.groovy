package suive.kotlinls

import org.junit.jupiter.api.Test

import java.nio.file.Files

class CompletionTest extends LanguageServerTest {

    @Test
    void "should receive completions"() {
        def workspaceRoot = createWorkspace("/test-projects/maven")
        def testClass = Files.createFile(
                workspaceRoot.resolve("src/main/kotlin/suive/kotlinls/testproject/TestClass.kt"))
        testClass << """
            package suive.kotlinls.testproject
            
            class TestClass {
                fun testMethod() {
                    val string = Opt
                }
            }
        """.stripIndent().trim()
        testEditor.initialize(workspaceRoot)

        def response = testEditor.request("textDocument/completion", [
                textDocument: [uri: testClass.toString()],
                position    : [line: 4, character: 27]
        ])
        assert response.isIncomplete == false
        assert response.items.contains("Option<T>") // From io.vavr
        assert response.items.contains("Optional<T>") // From java.util
    }
}
