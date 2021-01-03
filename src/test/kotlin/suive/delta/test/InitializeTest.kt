package suive.delta.test

import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.junit.jupiter.api.Test
import suive.delta.test.LanguageServerTest

class InitializeTest : LanguageServerTest() {
    @Test
    fun `should receive response to an initialize request`() {
        val response = testEditor.initialize(createWorkspace())
        assertThatJson(response) {
            node("result.capabilities").isNotNull
        }
    }
}
