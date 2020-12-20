package suive

import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.method.InitializeMethod
import suive.model.RequestMessage
import suive.model.ResponseMessage
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TcpServer(
    private val port: Int
) {
    private lateinit var serverSocket: ServerSocket
    private lateinit var client: Socket

    val worker: ExecutorService = Executors.newFixedThreadPool(1)

    var state: String = "NOT_STARTED"

    fun start() {
        serverSocket = ServerSocket(port)
        Logger.info { "Server socket started on port $port" }
        state = "STARTED"
        serverSocket.use { socket ->
            client = socket.accept()
            Logger.info { "Client connected." }
            state = "WAITING_FOR_COMMAND"
            val input = client.getInputStream()
            val output = client.getOutputStream()
            processStream(input).forEach {
                val message = JSON_MAPPER.readValue<RequestMessage<*>>(it)
                dispatch(message, output)
            }
        }
    }

    fun dispatch(message: RequestMessage<*>, outputStream: OutputStream) {
        val method = when (message.method) {
            "initialize" -> InitializeMethod()
            else -> return
        }
        val methodResult = method.process(message.params)
        val responseMessage = JSON_MAPPER.writeValueAsString(ResponseMessage.Success(message.id, methodResult))

        outputStream.write(
            """
            Content-Length: ${responseMessage.length}
            
            $responseMessage""".trimIndent().trim().toByteArray()
        )
    }

    fun stop() {
        client.close()
        serverSocket.close()
        Logger.info { "Client disconnected." }
    }
}
