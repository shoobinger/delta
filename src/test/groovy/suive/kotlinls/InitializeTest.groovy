package suive.kotlinls

import org.junit.jupiter.api.Test

class InitializeTest extends LanguageServerTest {

    @Test
    void "should receive response to an initialize"() {
        def response = testEditor.initialize(createWorkspace())
        assert response.result.capabilities != null
    }
}
