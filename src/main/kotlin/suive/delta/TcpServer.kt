package suive.delta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.delta.method.Request
import suive.delta.model.transport.RequestMessage
import java.net.ServerSocket
import java.net.Socket

class TcpServer(
    private val port: Int
) {
    private lateinit var serverSocket: ServerSocket
    private lateinit var client: Socket

    var state: String = "NOT_STARTED"

    private val jsonConverter = ObjectMapper().registerModule(KotlinModule())

    fun start() {
        serverSocket = ServerSocket(port)
        Logger.info { "Server socket started on port $port" }
        state = "STARTED"
        serverSocket.use { socket ->
            client = socket.accept()
            Logger.info { "Client connected." }
            state = "WAITING_FOR_COMMAND"
            val input = client.getInputStream()
            val methodDispatcher = MethodDispatcher(client.getOutputStream(), jsonConverter)

            Logger.info { "Started server (reading from stdin)" }
            for (i in processStream(input)) {
                val message = jsonConverter.readValue<RequestMessage>(i) // TODO this may be a notification.
                Logger.info { "Message received: $message" }

                if (message.method == "exit") {
                    break
                }

                try {
                    methodDispatcher.dispatch(Request(message.id ?: -1), message.method, message.params)
                } catch (e: Exception) {
                    Logger.error(e) { "Error in dispatcher" }
                }
            }
        }
    }

    fun stop() {
        client.close()
        serverSocket.close()
        Logger.info { "Client disconnected." }
    }
}
