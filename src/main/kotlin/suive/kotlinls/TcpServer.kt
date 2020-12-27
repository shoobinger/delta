package suive.kotlinls

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.kotlinls.method.Request
import suive.kotlinls.model.Output
import suive.kotlinls.model.transport.NotificationMessage
import suive.kotlinls.model.transport.RequestMessage
import suive.kotlinls.model.transport.ResponseMessage
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

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
            val clientHandle = ClientHandle(client.getOutputStream())
            val messages: BlockingQueue<Output> = ArrayBlockingQueue(1000)

            thread(start = true, name = "Sender") {
                while (!Thread.interrupted()) {
                    val response = when (val output = messages.take()) {
                        is Output.Result -> ResponseMessage.Success(output.request.requestId, output)
                        is Output.Notification<*> -> NotificationMessage(output)
                    }
                    // Use kotlinx.serialization
                    clientHandle.send(jsonConverter.writeValueAsString(response))
                }
            }

            processStream(input).forEach { i ->
                Logger.info { "Message received: $i" }
                val message = jsonConverter.readValue<RequestMessage>(i) // TODO this may be a notification.

                MethodDispatcher.dispatch(Request(message.id), message.method, message.params, messages::put)
            }
        }
    }

    fun stop() {
        client.close()
        serverSocket.close()
        Logger.info { "Client disconnected." }
    }
}
