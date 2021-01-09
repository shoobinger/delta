package suive.delta.test.completion

import org.junit.jupiter.api.Test
import java.nio.file.Files

class ExtensionsCompletionTest : CompletionTest() {

    @Test
    fun `should suggest extension functions`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            class A {}
            class TestClass {
                fun A.extensionFunction() {}
                fun testMethod() {
                    val a = A()
                    a.
                }
            }
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        testClass.assertCompletion(7, 10, listOf("extensionFunction"))
    }
}
