package suive.delta.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.tinylog.kotlin.Logger
import suive.delta.model.transport.Message
import suive.delta.model.transport.NotificationMessage
import suive.delta.model.transport.RequestMessage
import suive.delta.model.transport.ResponseMessage
import java.io.OutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class SenderService(
    private val outputStream: OutputStream,
    private val jsonConverter: ObjectMapper
) {
    private val messages: BlockingQueue<Message> = ArrayBlockingQueue(1000)

    private val idCounter = AtomicInteger(0)

    private val senderThread = thread(start = true, name = "Sender") {
        while (!Thread.interrupted()) {
            try {
                val message = messages.take()
                // TODO Use kotlinx.serialization
                val responseMessage = jsonConverter.writeValueAsString(message)

                val messageContent = "Content-Length: ${responseMessage.length}\r\n\r\n$responseMessage"
                Logger.info { "Sending message to client: $messageContent" }
                outputStream.write(messageContent.toByteArray())
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun send(message: Message) {
        messages.offer(message)
    }

    fun sendRequest(method: String, params: Any) =
        send(RequestMessage(idCounter.getAndIncrement(), method, params))

    fun sendNotification(method: String, params: Any) = send(NotificationMessage(method, params))

    fun sendResponse(requestId: Int, result: Any) = send(ResponseMessage.Success(requestId, result))
}
