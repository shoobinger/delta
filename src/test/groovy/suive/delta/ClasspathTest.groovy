package suive.delta

import org.junit.jupiter.api.Test

import java.nio.file.Files

class ClasspathTest extends LanguageServerTest {
    @Test
    void "should resolve classpath from Maven pom"() {
        def workspaceRoot = createWorkspace("/test-projects/maven")
        def testClass = Files.createFile(
                workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt"))
        testClass << """
            package suive.delta.testproject
            
            import io.vavr.control.Option
            
            class TestClass {
                fun testMethod() {
                    Option.of(1)
                }
            }
            """.stripIndent(true).trim()

        testEditor.initialize(workspaceRoot)

        assert testEditor.getNotification("textDocument/publishDiagnostics", 2) == null
    }
}
