package suive

import org.tinylog.kotlin.Logger
import java.io.OutputStream

class ClientHandle(
    private val outputStream: OutputStream
) {
    fun send(responseMessage: String) {
        Logger.info { "Sending message to client: $responseMessage" }
        outputStream.write(
            """
            Content-Length: ${responseMessage.length}
            
            $responseMessage""".trimIndent().trim().toByteArray()
        )
    }
}
