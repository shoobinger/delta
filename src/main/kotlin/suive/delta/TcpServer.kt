package suive.delta

import org.tinylog.kotlin.Logger
import java.net.ServerSocket
import java.net.Socket

class TcpServer(port: Int) {
    private val serverSocket: ServerSocket = ServerSocket(port)
    private lateinit var client: Socket
    private lateinit var server: Server

    fun stop() {
        client.close()
        serverSocket.close()
        Logger.info { "Client disconnected." }
    }

    fun start() {
        serverSocket.use { socket ->
            client = socket.accept()
            Logger.info { "Client connected." }
            server = Server(client.getInputStream(), client.getOutputStream())
        }
        server.start()
    }
}
