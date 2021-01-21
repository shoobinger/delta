package suive.delta.test.completion

import org.junit.jupiter.api.Test
import java.nio.file.Files

class ClassCompletionTest : CompletionTest() {

    @Test
    fun `should complete types from jdk`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            class SomeException : RuntExc
            val str: Str
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        Thread.sleep(2000) // TODO wait for a notification
        testClass.assertCompletion(2, 29, listOf("RuntimeException"))
        testClass.assertCompletion(3, 12, listOf("String"))
        testClass.assertCompletion(4, 17, listOf("ByteArray"))
    }

    @Test
    fun `should complete types from classpath`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )

        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            val byteArray: BA
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        Thread.sleep(2000) // TODO wait for a notification
        testClass.assertCompletion(2, 29, listOf("RuntimeException"))
    }

    @Test
    fun `should complete types from workspace`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")

        val fileA = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/A.kt")
        )

        Files.writeString(
            fileA, """
            package suive.delta.testproject
            
            interface SomeInterface
        """.trimIndent()
        )

        val fileB = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/B.kt")
        )

        Files.writeString(
            fileB, """
            package suive.delta.testproject
            
            class SomeClass : SomeInterface
        """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        Thread.sleep(40000) // TODO wait for a notification
//        fileB.assertCompletion(2, , listOf("SomeInterface"))
    }
}
