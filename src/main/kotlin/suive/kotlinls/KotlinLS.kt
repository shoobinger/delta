package suive.kotlinls

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

fun main() {
    KotlinLS().startServer(8500)
}
