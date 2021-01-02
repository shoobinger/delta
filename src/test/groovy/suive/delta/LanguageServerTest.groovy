package suive.delta

import groovy.io.FileType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class LanguageServerTest {

    protected TcpServer languageServer
    protected TestEditor testEditor

    final int LANGUAGE_SERVER_PORT = 8500 // TODO Should be selected randomly.

    @BeforeAll
    void setup() {
        languageServer = new TcpServer(LANGUAGE_SERVER_PORT)
        def serverThread = new Thread({ languageServer.start() })
        serverThread.start()
        Thread.sleep(50)

        testEditor = new TestEditor(LANGUAGE_SERVER_PORT)
    }

    @AfterAll
    void shutdown() {
        testEditor.stopSession()
        languageServer.stop()
    }

    protected static def createWorkspace(String resource = null) {
        def root = Files.createTempDirectory("delta-test-workspace")

        if (resource != null) {
            copyDirectory(Paths.get(LanguageServerTest.class.getResource(resource).toURI()).toFile(), root.toFile())
        }
        root
    }

    protected static void copyDirectory(File dirFrom, File dirTo) {
        if (!dirTo.exists()) {
            dirTo.mkdir()
        }
        dirFrom.eachFile(FileType.FILES) { File source ->
            File target = new File(dirTo, source.getName())
            target.bytes = source.bytes
        }

        dirFrom.eachFile(FileType.DIRECTORIES) { File source ->
            File target = new File(dirTo, source.getName())
            copyDirectory(source, target)
        }
    }
}
