package suive.delta.test.completion

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.UUID

class PartialResultCompletionTest : CompletionTest() {

    @Test
    fun `should send completions in steps`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            class A {
                fun function() {}
            }
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

        val partialResultToken = UUID.randomUUID().toString()
        val response = sendCompletionRequest(testClass, 9, 10, partialResultToken)

        val firstNotification = testEditor.getNotification("$/progress")
        assertJson(firstNotification) {
            node("params.token").isEqualTo(partialResultToken)
            assertCompletionItems(
                node("params.value.items").isArray,
                listOf("function")
            ) // First partial result with type members.
        }
        val secondNotification = testEditor.getNotification("$/progress")
        assertJson(secondNotification) {
            node("params.token").isEqualTo(partialResultToken)
            assertCompletionItems(
                node("params.value.items").isArray,
                listOf("extensionFunction")
            ) // Second partial result with extensions.
        }

        // Final result is null.
        assertJson(response) {
            node("result.items").isNotNull.isArray.isEmpty()
        }
    }
}
