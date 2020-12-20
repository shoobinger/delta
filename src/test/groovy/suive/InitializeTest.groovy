package suive


import org.junit.jupiter.api.Test

class InitializeTest extends LanguageServerTest {

    @Test
    void "should receive response to an initialize"() {
        def response = request("initialize", [processId: null, rootUri: "file:///home/someone/projects/project"])
        assert response.result.capabilities != null
    }
}
