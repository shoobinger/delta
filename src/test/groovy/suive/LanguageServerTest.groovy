package suive

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout

import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(2L)
abstract class LanguageServerTest {

    protected KotlinLS languageServer
    protected Socket client
    protected InputStream inputStream

    @BeforeAll
    void setup() {
        languageServer = new KotlinLS()
        def serverThread = new Thread({ languageServer.startServer(8500) })
        serverThread.start()
        Thread.sleep(50)
        client = new Socket("localhost", 8500)
        inputStream = client.inputStream
    }

    @AfterAll
    void shutdown() {
        client.close()
        languageServer.stopServer()
    }

    protected def request(String method, Object params) {
        def messageId = send(method, params)
        def responseMessage = readOneMessage()
        assert responseMessage.id == messageId
        responseMessage
    }

    protected Map readOneMessage() {
        def text = TcpKt.processStream(inputStream).iterator().next()
        new JsonSlurper().parseText(text) as Map
    }

    protected String send(String method, Object params) {
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

    protected def createWorkspace() {
        def root = Files.createTempDirectory("kotlin-ls-test-workspace")
        root.toFile().deleteOnExit()

        root
    }
}
