package suive


import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout

import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(2L)
abstract class LanguageServerTest {

    protected KotlinLS languageServer
    protected TestEditor testEditor

    final int LANGUAGE_SERVER_PORT = 8500 // TODO Should be selected randomly.

    @BeforeAll
    void setup() {
        languageServer = new KotlinLS()
        def serverThread = new Thread({ languageServer.startServer(LANGUAGE_SERVER_PORT) })
        serverThread.start()
        Thread.sleep(50)

        testEditor = new TestEditor(LANGUAGE_SERVER_PORT)
    }

    @AfterAll
    void shutdown() {
        testEditor.stopSession()
        languageServer.stopServer()
    }

    protected static def createWorkspace() {
        def root = Files.createTempDirectory("kotlin-ls-test-workspace")
        root.toFile().deleteOnExit()

        root
    }
}
