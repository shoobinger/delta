package suive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.method.Request
import suive.model.transport.NotificationMessage
import suive.model.Output
import suive.model.transport.RequestMessage
import suive.model.transport.ResponseMessage
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread


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
            val clientHandle = ClientHandle(client.getOutputStream())
            val messages: BlockingQueue<Output> = ArrayBlockingQueue(1000)

            thread(start = true, name = "Sender") {
                while (!Thread.interrupted()) {
                    val response = when (val output = messages.take()) {
                        is Output.Result -> ResponseMessage.Success(output.request.requestId, output)
                        is Output.Notification<*> -> NotificationMessage(output)
                    }
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
