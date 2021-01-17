package suive.delta.test.completion

import org.junit.jupiter.api.Test
import java.nio.file.Files

class ClassCompletionTest : CompletionTest() {

    @Test
    fun `should complete classes in class definitions`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            class SomeException : Exc
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        testClass.assertCompletion(2, 25, listOf("Exception"))
    }
}
