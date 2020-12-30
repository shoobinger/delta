package suive.kotlinls

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.kotlinls.method.Request
import suive.kotlinls.model.Output
import suive.kotlinls.model.transport.NotificationMessage
import suive.kotlinls.model.transport.RequestMessage
import suive.kotlinls.model.transport.ResponseMessage
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

class StdStreamServer {
    var state: String = "NOT_STARTED"

    private val jsonConverter = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun start() {
        val input = System.`in`
        val clientHandle = ClientHandle(System.out)
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

        Logger.info { "Started server (reading from stdin)" }
        processStream(input).forEach { i ->
            val message = jsonConverter.readValue<RequestMessage>(i) // TODO this may be a notification.
            Logger.info { "Message received: $message" }

            try {
                MethodDispatcher.dispatch(Request(message.id), message.method, message.params, messages::put)
            } catch (e: Exception) {
                Logger.error(e) { "Error in dispatcher" }
            }
        }
    }
}
