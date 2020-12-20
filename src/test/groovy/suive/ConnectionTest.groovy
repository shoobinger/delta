package suive

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitializeSpecification {

    KotlinLS languageServer
    Socket client
    InputStream inputStream
    OutputStream outputStream

    @BeforeAll
    void setup() {
        languageServer = new KotlinLS()
        def serverThread = new Thread({ languageServer.startServer(8500) })
        serverThread.start()
        client = new Socket("localhost", 8500)
        inputStream = client.inputStream
    }

    @AfterAll
    void shutdown() {
        client.close()
        languageServer.stopServer()
    }

    @Test
    void "should receive response to an initialize"() {
        def response = request("initialize", [processId: null, rootUri: "file:///home/someone/projects/project"])
        assert response.result.capabilities != null
    }

    private def request(String method, Object params) {
        def messageId = send(method, params)
        def text = readOneMessage()
        def message = new JsonSlurper().parseText(text)
        assert message.id == messageId
        message
    }

    private String readOneMessage() {
        TcpKt.processStream(inputStream).iterator().next()
    }

    private String send(String method, Object params) {
        def messageId = UUID.randomUUID().toString()
        def message = JsonOutput.toJson([
                jsonrpc: "2.0",
                id     : messageId,
                method : method,
                params : params
        ])

        client << """
             Content-Length: ${message.size()}\r\n\r\n$message
        """.stripIndent().trim()

        messageId
    }
}
