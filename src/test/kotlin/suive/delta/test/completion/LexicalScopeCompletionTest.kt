package suive.delta.test.completion

import org.junit.jupiter.api.Test
import java.nio.file.Files

/**
 * Tests for completing simple variables and functions from the lexical scope.
 */
class LexicalScopeCompletionTest : CompletionTest() {
    @Test
    fun `should resolve lexical scope variables`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            val someClassLevelVariable = Any()
            
            class TestClass {
                companion object {
                    const val SOME_CONST = 1L
                }
                fun localVariable() {
                    val someString = ""
                    so
                }
                fun classMethod() {
                    loc
                }
                fun companionObjectVal() {
                    SO
                }
                fun classLevelVariable() {
                    someCl
                }
            }
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        testClass.assertCompletion(10, 10, listOf("someString: String"))
        testClass.assertCompletion(13, 11, listOf("localVariable(): Unit"))
        testClass.assertCompletion(16, 10, listOf("SOME_CONST: Long"))
        testClass.assertCompletion(19, 14, listOf("someClassLevelVariable: Any"))
    }
}
