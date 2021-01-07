package suive.delta.test

import org.junit.jupiter.api.Test

class InitializeTest : LanguageServerTest() {
    @Test
    fun `should receive response to an initialize request`() {
        val response = testEditor.initialize(createWorkspace())
        assertJson(response) {
            node("result.capabilities").isNotNull
        }
    }
}
