package suive.kotlinls

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import suive.kotlinls.BlockingMap

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test code editor that functions like an LSP client.
 */
class TestEditor {
    private Socket client
    private InputStream inputStream

    private Path workspacePath
    private int port

    private BlockingMap<Integer, Map> responses = new BlockingMap()
    private BlockingMap<String, Map> notifications = new BlockingMap()

    private int row = 1
    private int col = 1

    private final int GET_NOTIFICATION_TIMEOUT_SEC = 120

    private AtomicInteger messageId = new AtomicInteger(0)

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
                    responses.set(message.id as Integer, message)
                } else {
                    throw new IllegalStateException("Unknown message received")
                }
            }
        }).start()

        request("initialize", [processId: null, rootUri: "file://${workspacePath.toAbsolutePath()}"])
    }

    Map getResponse(Integer messageId) {
        responses.get(messageId) as Map
    }

    Map getNotification(String method) {
        notifications.get(method, GET_NOTIFICATION_TIMEOUT_SEC, TimeUnit.SECONDS) as Map
    }

    protected def request(String method, Object params) {
        def messageId = send(method, params)
        getResponse(messageId)
    }

    protected def sendNotification(String method, Object params) {
        send(method, params)
    }

    protected Integer send(String method, Object params) {
        def id = messageId.getAndIncrement()
        def message = JsonOutput.toJson([
                jsonrpc: "2.0",
                id     : id,
                method : method,
                params : params
        ])

        client << """
             Content-Length: ${message.size()}\r\n\r\n$message
        """.stripIndent().trim()

        id
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
