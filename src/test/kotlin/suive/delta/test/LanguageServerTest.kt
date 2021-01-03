package suive.delta.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import suive.delta.TcpServer
import suive.delta.TestEditor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.concurrent.thread
import kotlin.contracts.contract
import java.io.IOException
import java.lang.IllegalStateException

import java.net.ServerSocket

abstract class LanguageServerTest {

    private lateinit var languageServer: TcpServer
    protected lateinit var testEditor: TestEditor

    @BeforeEach
    fun setup() {
        val port = findFreePort()
        languageServer = TcpServer(port)
        thread(start = true) { languageServer.start() }
        Thread.sleep(50)

        testEditor = TestEditor(port)
    }

    @AfterEach
    fun shutdown() {
        testEditor.stopSession()
        languageServer.stop()
    }

    protected fun createWorkspace(resource: String? = null): Path {
        val root = Files.createTempDirectory("delta-test-workspace")

        if (resource != null) {
            copyDirectory(Paths.get(LanguageServerTest::class.java.getResource(resource).toURI()), root)
        }
        return root
    }

    private fun copyDirectory(dirFrom: Path, dirTo: Path) {
        Files.walk(dirFrom).forEach {
            val p = dirTo.resolve(dirFrom.relativize(it))
            if (Files.isDirectory(it)) {
                Files.createDirectories(p)
            } else {
                Files.copy(it, p)
            }
        }
    }

    protected fun <T : Any?> assertNotNull(a: T?): T? {
        contract {
            returns() implies (a != null)
        }
        Assertions.assertNotNull(a)
        return a
    }

    private fun findFreePort(): Int {
        var socket: ServerSocket? = null
        try {
            socket = ServerSocket(0)
            socket.reuseAddress = true
            val port = socket.localPort
            try {
                socket.close()
            } catch (e: IOException) {
                // Ignore IOException on close()
            }
            return port
        } catch (e: IOException) {
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (e: IOException) {
                }
            }
        }
        throw IllegalStateException("Could not find a free TCP/IP port")
    }
}
