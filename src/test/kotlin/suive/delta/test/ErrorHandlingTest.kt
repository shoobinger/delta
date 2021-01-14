package suive.delta.test

import org.junit.jupiter.api.Test
import suive.delta.getTempDir
import suive.delta.model.InternalError
import suive.delta.model.InvalidParams
import suive.delta.model.InvalidRequest
import suive.delta.model.MethodNotFound
import suive.delta.model.ParseError
import java.nio.file.Paths
import java.util.UUID

class ErrorHandlingTest : LanguageServerTest() {
    @Test
    fun `should respond with error if workspace dir can't be found`() {
        val nonexistentDir = Paths.get(getTempDir(), UUID.randomUUID().toString())
        val response = testEditor.initialize(nonexistentDir)
        assertJson(response) {
            node("error").isNotNull.node("code").isEqualTo(InternalError)
        }
    }

    @Test
    fun `should respond with error if the request is not a valid JSON`() {
        testEditor.initialize(createWorkspace())

        testEditor.write("Some invalid data {}123")
        val response = testEditor.getResponse(-1)

        assertJson(response) {
            node("error").isNotNull.node("code").isEqualTo(ParseError)
        }
    }

    @Test
    fun `should respond with error if the request is a valid JSON but not a valid request object`() {
        testEditor.initialize(createWorkspace())

        testEditor.write("{}")
        val response = testEditor.getResponse(-1)

        assertJson(response) {
            node("error").isNotNull.node("code").isEqualTo(InvalidRequest)
        }
    }

    @Test
    fun `should respond with error if method does not exist`() {
        testEditor.initialize(createWorkspace())

        val response = testEditor.request("some-non-existing-method", "{}")

        assertJson(response) {
            node("error").isNotNull.node("code").isEqualTo(MethodNotFound)
        }
    }

    @Test
    fun `should respond with error if params are invalid`() {
        testEditor.initialize(createWorkspace())

        val response = testEditor.request("textDocument/didChange", "{}")

        assertJson(response) {
            node("error").isNotNull.node("code").isEqualTo(InvalidParams)
        }
    }
}
