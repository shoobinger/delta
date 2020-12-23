package suive

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.nio.file.Files
import java.nio.file.Path

/**
 * Test code editor that functions like an LSP client.
 */
class TestEditor {
    private Socket client
    private InputStream inputStream

    private Path workspacePath
    private int port

    private BlockingMap<String, Map> responses = new BlockingMap()
    private BlockingMap<String, Map> notifications = new BlockingMap()

    private int row = 1
    private int col = 1

    TestEditor(int port) {
        this.port = port
    }

    Map initialize(Path workspacePath) {
        this.workspacePath = workspacePath

        client = new Socket("localhost", port)
        inputStream = client.inputStream

        new Thread({
            TcpKt.processStream(inputStream).each {
                def message = new JsonSlurper().parseText(it) as Map
                if (message.method != null) {
                    notifications.set(message.method as String, message)
                } else if (message.id != null) {
                    responses.set(message.id as String, message)
                } else {
                    throw new IllegalStateException("Unknown message received")
                }
            }
        }).start()

        request("initialize", [processId: null, rootUri: workspacePath.toAbsolutePath().toString()])
    }

    Map getResponse(String messageId) {
        responses.get(messageId) as Map
    }

    Map getNotification(String method) {
        notifications.get(method) as Map
    }

    protected def request(String method, Object params) {
        def messageId = send(method, params)
        getResponse(messageId)
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

    def stopSession() {
        if (client != null && !client.isClosed()) {
            client.close()
        }
    }

    def moveCursor(int row, int col) {
        this.row = row
        this.col = col
    }

    def write(String file, String s) {
        def path = workspacePath.resolve(file)
        if (!Files.exists(path)) {
            Files.createFile(path)
        }
        path << s
    }
}
