package suive.delta

import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.nio.file.Files

class ClasspathTest : LanguageServerTest() {
    @Test
    fun `should resolve classpath from Maven pom`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )

        val pom = workspaceRoot.resolve("pom.xml")
        Files.writeString(
            pom, Files.readString(pom).replace(
                "</dependencies>", """
            <dependency>
                <groupId>io.vavr</groupId>
                <artifactId>vavr</artifactId>
                <version>0.10.3</version>
            </dependency>
        </dependencies>
        """
            )
        )
        Files.writeString(
            testClass, """
            package suive.delta.testproject
            
            import io.vavr.control.Option
            
            class TestClass {
                fun testMethod() {
                    Option.of(1)
                }
            }
            """
        )

        testEditor.initialize(workspaceRoot)

        assertThat(testEditor.getNotification("textDocument/publishDiagnostics", 2)).isNull()
    }

    @Test
    fun `should rebuild workspace after changing classpath`() {
        val workspaceRoot = createWorkspace("/test-projects/maven")
        val testClass = Files.createFile(
            workspaceRoot.resolve("src/main/kotlin/suive/delta/testproject/TestClass.kt")
        )
        Files.writeString(
            testClass,
            """
            package suive.delta.testproject
            
            import io.vavr.control.Option
            
            class TestClass {
                fun testMethod() {
                    Option.of(1)
                }
            }
            """.trimIndent()
        )

        testEditor.initialize(workspaceRoot)

        // Vavr is not in the classpath, should receive build errors.
        val diagnosticNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(diagnosticNotification)
        assertThatJson(diagnosticNotification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSizeGreaterThan(1)
            node("params.diagnostics[0].message").asString().contains("Unresolved reference")
        }

        // Client should receive dynamic registration request to receive pom.xml change notifications.
        // TODO

        // Add missing dependency.
        val pom = workspaceRoot.resolve("pom.xml")
        Files.writeString(
            pom, Files.readString(pom).replace(
                "</dependencies>", """
            <dependency>
                <groupId>io.vavr</groupId>
                <artifactId>vavr</artifactId>
                <version>0.10.3</version>
            </dependency>
        </dependencies>
        """.trimIndent()
            )
        )

        // Send didChangeWatchedFiles.
        testEditor.sendNotification(
            "workspace/didChangeWatchedFiles",
            """
                {
                  "changes": {
                    "uri": "${pom.toUri()}",
                    "type": 2
                  }
                }
            """.trimIndent()
        )

        // Build should succeed.
        val secondNotification = testEditor.getNotification("textDocument/publishDiagnostics")
        assertNotNull(secondNotification)
        assertThatJson(secondNotification) {
            node("params.uri").isEqualTo(testClass.toUri().toString())
            node("params.diagnostics").isArray.hasSize(0)
        }
    }
}
