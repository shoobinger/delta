package suive.delta

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.delta.model.transport.RequestMessage

class StdStreamServer {
    private val jsonConverter = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun start() {
        val input = System.`in`
        val methodDispatcher = MethodDispatcher(System.out, jsonConverter)

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
