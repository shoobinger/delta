package suive.delta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

typealias Json = String

/**
 * Test code editor that functions like an LSP client.
 */
class TestEditor(private val port: Int) {
    private lateinit var client: Socket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    private lateinit var workspacePath: Path

    private val responses: BlockingMap<Int, Json> = BlockingMap()
    private val requests: BlockingMap<String, Json> = BlockingMap()
    private val notifications: BlockingMap<String, Json> = BlockingMap()

    companion object {
        const val GET_NOTIFICATION_TIMEOUT_SEC = 5L
    }

    private val messageId = AtomicInteger(0)
    private val objectMapper = ObjectMapper()

    fun initialize(workspacePath: Path): Json? {
        this.workspacePath = workspacePath

        client = Socket("localhost", port)
        inputStream = client.inputStream
        outputStream = client.getOutputStream()

        thread(start = true) {
            try {
                processStream(inputStream).forEach {
                    val message = objectMapper.readValue<Map<*, *>>(it)
                    when {
                        message["method"] != null && message["id"] != null -> requests[message["method"] as String] = it
                        message["method"] != null -> notifications[message["method"] as String] = it
                        message["id"] != null -> responses[message["id"] as Int] = it
                        else -> error("Unknown message received")
                    }
                }
            } catch (e: IOException) {
                // Ignore socket close exception.
            }
        }

        return request(
            "initialize",
            """{"processId": null, "rootUri": "file://${workspacePath.toAbsolutePath()}"}"""
        )
    }

    fun getResponse(messageId: Int): Json? = responses.get(messageId, GET_NOTIFICATION_TIMEOUT_SEC, TimeUnit.SECONDS)

    fun getRequest(method: String) = requests[method]

    fun getNotification(method: String, timeout: Long = GET_NOTIFICATION_TIMEOUT_SEC): Json? {
        return notifications.get(method, timeout, TimeUnit.SECONDS)
    }

    fun request(method: String, params: String): Json? {
        val messageId = send(method, params)
        return getResponse(messageId)
    }

    fun sendNotification(method: String, params: String) {
        send(method, params)
    }

    protected fun send(method: String, params: String): Int {
        val id = messageId.getAndIncrement()
        val message = """
            {
                "jsonrpc": "2.0",
                "id": $id,
                "method": "$method",
                "params": $params
            }
        """.trimIndent()

        outputStream.write("Content-Length: ${message.length}\r\n\r\n$message".toByteArray())
        return id
    }

    fun stopSession() {
        if (::client.isInitialized && !client.isClosed) {
            client.close()
        }
    }
/*

    void editFile(Path path, String content) {
        Object oldContent = Files.readString(path)
        Object newContent = content.stripIndent(true).trim()
        Object minLength = Math.min(newContent.length(), oldContent.length())

        Object rangeStart = 0
        Object lineStart = 0
        Object charStart = 0
        for (i in 0..<minLength) {
            if (oldContent[i] == newContent[i]) {
                rangeStart++
                charStart++
                if (oldContent[i] == '\n') {
                    charStart = 0
                    lineStart++
                }
            } else {
                break
            }
        }

        Object rangeEnd = 0
        Object lineEnd = 0
        Object charEnd = 0
        for (i in 0..<minLength) {
            if (oldContent[oldContent.length() - i - 1] == newContent[newContent.length() - i - 1]) {
                rangeEnd++
                charEnd++
                if (oldContent[oldContent.length() - i - 1] == '\n') {
                    charEnd = 0
                    lineEnd++
                }
            } else {
                lineEnd = oldContent.readLines().size() - lineEnd - 1
                charEnd = oldContent.readLines()[lineEnd].size() - charEnd
                rangeEnd = oldContent.size() - rangeEnd
                break
            }
        }

        Object text = oldContent.substring(rangeStart, rangeEnd)
        print("a")
    }
*/

}
