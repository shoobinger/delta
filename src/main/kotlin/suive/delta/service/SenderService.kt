package suive.delta.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.tinylog.kotlin.Logger
import suive.delta.model.NullResult
import suive.delta.model.Output
import suive.delta.model.transport.NotificationMessage
import suive.delta.model.transport.ResponseMessage
import java.io.OutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

class SenderService(
    private val outputStream: OutputStream,
    private val jsonConverter: ObjectMapper
) {
    private val messages: BlockingQueue<Output> = ArrayBlockingQueue(1000)

    private val senderThread = thread(start = true, name = "Sender") {
        while (!Thread.interrupted()) {
            try {
                val response = when (val output = messages.take()) {
                    is NullResult -> ResponseMessage.Success(output.request.requestId, null)
                    is Output.Result -> ResponseMessage.Success(output.request.requestId, output)
                    is Output.Notification<*> -> NotificationMessage(output)
                }
                // TODO Use kotlinx.serialization
                val responseMessage = jsonConverter.writeValueAsString(response)

                val message = "Content-Length: ${responseMessage.length}\r\n\r\n$responseMessage"
                Logger.info { "Sending message to client: $message" }
                outputStream.write(message.toByteArray())
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun send(output: Output) {
        messages.offer(output)
    }
}
