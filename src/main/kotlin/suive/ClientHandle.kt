package suive

import java.io.OutputStream

class ClientHandle(
    private val outputStream: OutputStream
) {
    fun send(responseMessage: String) {
        outputStream.write(
            """
            Content-Length: ${responseMessage.length}
            
            $responseMessage""".trimIndent().trim().toByteArray()
        )
    }
}
