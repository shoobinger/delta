package suive.delta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.file.Path
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
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
    private val notifications = ConcurrentHashMap<String, BlockingQueue<Json>>()

    companion object {
        const val GET_NOTIFICATION_TIMEOUT_SEC = 15L
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
                        message["method"] != null -> notifications.compute(message["method"] as String) { _, q ->
                            val queue = (q ?: LinkedBlockingQueue())
                            queue.offer(it)
                            queue
                        }
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

    fun getNotification(method: String, timeout: Long = GET_NOTIFICATION_TIMEOUT_SEC): Json? =
        notifications.computeIfAbsent(method) { LinkedBlockingQueue() }.poll(timeout, TimeUnit.SECONDS)

    fun request(method: String, params: String): Json? {
        val messageId = send(method, params)
        return getResponse(messageId)
    }

    fun sendNotification(method: String, params: String) {
        send(method, params)
    }

    fun send(method: String, params: String): Int {
        val id = messageId.getAndIncrement()
        val message = """
            {
                "jsonrpc": "2.0",
                "id": $id,
                "method": "$method",
                "params": $params
            }
        """.trimIndent()

        write(message)
        return id
    }

    fun write(message: String) {
        outputStream.write("Content-Length: ${message.length}\r\n\r\n$message".toByteArray())
    }

    fun stopSession() {
        if (::client.isInitialized && !client.isClosed) {
            client.close()
        }
    }
}
