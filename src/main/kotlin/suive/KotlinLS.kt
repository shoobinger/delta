package suive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class KotlinLS {
    lateinit var tcpServer: TcpServer
    fun startServer(port: Int) {
        tcpServer = TcpServer(port)
        tcpServer.start()
    }

    fun stopServer() {
        tcpServer.stop()
    }
}

val JSON_MAPPER = ObjectMapper().registerModule(KotlinModule())

fun main() {
    KotlinLS().startServer(8500)
}
