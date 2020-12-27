package suive.kotlinls

import groovy.io.FileType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Timeout(2L)
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

    protected static def createWorkspace(String resource = null) {
        def root = Files.createTempDirectory("kotlin-ls-test-workspace")
        root.toFile().deleteOnExit()

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
