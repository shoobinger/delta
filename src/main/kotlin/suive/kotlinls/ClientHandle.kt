package suive.kotlinls

import org.tinylog.kotlin.Logger
import java.io.OutputStream

class ClientHandle(
    private val outputStream: OutputStream
) {
    fun send(responseMessage: String) {
        val message = "Content-Length: ${responseMessage.length}\r\n\r\n$responseMessage"
        Logger.info { "Sending message to client: $message" }
        outputStream.write(
                message.toByteArray()
            )
    }
}
