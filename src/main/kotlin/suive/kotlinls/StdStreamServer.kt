package suive.kotlinls

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.kotlin.fir.types.coneFlexibleOrSimpleType
import org.tinylog.kotlin.Logger
import suive.kotlinls.method.Request
import suive.kotlinls.model.NullResult
import suive.kotlinls.model.Output
import suive.kotlinls.model.transport.NotificationMessage
import suive.kotlinls.model.transport.RequestMessage
import suive.kotlinls.model.transport.ResponseMessage
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread
import kotlin.system.exitProcess

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

        val senderThread = thread(start = true, name = "Sender") {
            while (!Thread.interrupted()) {
                try {
                    val response = when (val output = messages.take()) {
                        is NullResult -> ResponseMessage.Success(output.request.requestId, null)
                        is Output.Result -> ResponseMessage.Success(output.request.requestId, output)
                        is Output.Notification<*> -> NotificationMessage(output)
                    }
                    // Use kotlinx.serialization
                    clientHandle.send(jsonConverter.writeValueAsString(response))
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        Logger.info { "Started server (reading from stdin)" }
        for (i in processStream(input)) {
            val message = jsonConverter.readValue<RequestMessage>(i) // TODO this may be a notification.
            Logger.info { "Message received: $message" }

            if (message.method == "exit") {
                break
            }

            if (message.id == null) {
                // Notifications are not yet supported.
                continue
            }

            try {
                MethodDispatcher.dispatch(Request(message.id), message.method, message.params, messages::put)
            } catch (e: Exception) {
                Logger.error(e) { "Error in dispatcher" }
            }
        }

        senderThread.interrupt()
        exitProcess(0)
    }
}
