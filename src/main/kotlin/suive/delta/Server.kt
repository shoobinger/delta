package suive.delta

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.delta.model.NoParams
import suive.delta.model.transport.Message
import suive.delta.model.transport.ResponseMessage
import suive.delta.model.transport.WithId
import suive.delta.model.transport.WithMethod
import suive.delta.model.transport.WithParams
import java.io.InputStream
import java.io.OutputStream

open class Server(private val inputStream: InputStream, private val outputStream: OutputStream) {
    private val jsonConverter = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun start() {
        val methodDispatcher = MethodDispatcher(outputStream, jsonConverter)

        Logger.info { "Started server" }
        for (i in processStream(inputStream)) {
            try {
                val message = jsonConverter.readValue<Message>(i)
                Logger.info { "Message received: $message" }

                if (message is ResponseMessage) continue

                val method = if (message is WithMethod) message.method else "N/A"
                val id = if (message is WithId) message.id else -1
                val params = if (message is WithParams) message.params else NoParams

                if (method == "exit") {
                    break
                }

                methodDispatcher.dispatch(Request(id), method, params)
            } catch (e: Exception) {
                Logger.error(e) { "Error in dispatcher" }
            }
        }
    }
}
