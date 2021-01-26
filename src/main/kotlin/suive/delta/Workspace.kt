package suive.delta

import org.tinylog.kotlin.Logger
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

class Workspace {

    data class Config(
        val jdkPath: Path
    )

    lateinit var externalRoot: Path
    val internalRoot: Path = Paths.get(getTempDir(), "delta-workspace-${UUID.randomUUID()}").apply {
        Files.createDirectories(this)
    }

    @Volatile
    var classpath: List<File> = emptyList()

    var config: Config = readConfig()

    val jdkClassesPath
        get() = internalRoot.resolve(".jdk") // TODO prevent name collision

    private fun readConfig(): Config {
        val configFile = externalRoot.resolve(".deltarc")
        val configFromFile = if (Files.exists(configFile)) configFile.toFile().readLines()
            .map { it.split(" ") }.associateBy({ it[0] }, { it[1] }) else emptyMap()

        val jdkPath = configFromFile["jdkPath"] ?: System.getenv("JAVA_HOME")

        if (!jdkPath.isNullOrEmpty())
            resolveJdkClasses(jdkPath)

        return Config(jdkPath = Paths.get(jdkPath))
    }

    private fun resolveJdkClasses(jdkPath: String) {
        // If JDK version >= 9, unzip jmod
        unzip(Paths.get(jdkPath, "jmods", "java.base.jmod"), jdkClassesPath.resolve("java.base"), "/classes")

        jdkClassesPath
        // else use rt.jar
    }

    fun initialize(externalRoot: Path) {
        this.externalRoot = externalRoot
        Files.walk(externalRoot).forEach {
            val p = internalRoot.resolve(externalRoot.relativize(it).toString())
            if (Files.isDirectory(it)) {
                Files.createDirectories(p)
            } else {
                Files.copy(it, p)
            }
        }
    }

    fun toInternalPath(fileUri: String): Path = internalRoot.resolve(externalRoot.relativize(Paths.get(URI(fileUri))))

    fun toExternalUri(internalPath: Path): String =
        "file://" + externalRoot.resolve(internalRoot.relativize(internalPath))

    fun updateClasspath(paths: List<Path>) {
        classpath = paths.map { it.toFile() }
        Logger.info { "Classpath updated, new classpath $classpath" }
    }
}
