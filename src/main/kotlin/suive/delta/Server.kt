package suive.delta

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.tinylog.kotlin.Logger
import suive.delta.model.InvalidRequest
import suive.delta.model.NoParams
import suive.delta.model.ParseError
import suive.delta.model.transport.Message
import suive.delta.model.transport.ResponseError
import suive.delta.model.transport.ResponseMessage
import suive.delta.model.transport.WithId
import suive.delta.model.transport.WithMethod
import suive.delta.model.transport.WithParams
import suive.delta.service.Sender
import suive.delta.util.InvalidRequestException
import java.io.InputStream
import java.io.OutputStream

open class Server(private val inputStream: InputStream, private val outputStream: OutputStream) {
    private val jsonConverter = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val sender = Sender(outputStream, jsonConverter)

    fun start() {
        val dispatcher = Dispatcher(sender)

        Logger.info { "Started server" }
        for (input in processStream(inputStream)) {
            try {
                Logger.trace { "Incoming message [$input]" }
                val message = try {
                    jsonConverter.readValue<Message>(input)
                } catch (e: JsonParseException) {
                    Logger.error(e) { "Can't parse incoming message [$input]" }
                    sender.send(ResponseMessage.Error(-1, ResponseError(ParseError, e.message ?: "N/A")))
                    continue
                } catch (e: InvalidRequestException) {
                    Logger.error(e) { "Invalid JSON-RPC request [$input]" }
                    sender.send(ResponseMessage.Error(-1, ResponseError(InvalidRequest, e.message ?: "N/A")))
                    continue
                }
                Logger.info { "Message received: $message" }

                if (message is ResponseMessage) continue

                val method = if (message is WithMethod) message.method else "N/A"
                val id = if (message is WithId) message.id else -1
                val params = if (message is WithParams) message.params else NoParams

                if (method == "exit") {
                    break
                }

                dispatcher.dispatch(Request(id), method, params)
            } catch (e: Exception) {
                Logger.error(e) { "Error in dispatcher" }
            }
        }
    }
}
