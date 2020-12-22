package suive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.model.NotificationMessage
import suive.model.RequestMessage
import suive.model.ResponseMessage
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
            val output = client.getOutputStream()
            val clientHandle = ClientHandle(output)
            processStream(input).forEach {
                Logger.info { "Message received: $it" }
                val message = jsonConverter.readValue<RequestMessage>(it) // TODO this may be a notification.

                Application.dispatch(
                    methodName = message.method,
                    paramsRaw = message.params,
                    handleResult = { output ->
                        val responseMessage =
                            jsonConverter.writeValueAsString(ResponseMessage.Success(message.id, output))
                        clientHandle.send(responseMessage)
                    },
                    handleNotification = { method, params ->
                        val responseMessage = jsonConverter.writeValueAsString(NotificationMessage(method, params))
                        clientHandle.send(responseMessage)
                    },
                    handleError = {
                        // TODO handle error
                    }
                )
            }
        }
    }

    fun stop() {
        client.close()
        serverSocket.close()
        Logger.info { "Client disconnected." }
    }
}
