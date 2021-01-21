package suive.delta

import org.tinylog.kotlin.Logger
import suive.delta.model.TextDocumentContentChangeEvent
import suive.delta.util.getOffset
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

class Workspace {

    lateinit var externalRoot: Path
    val internalRoot = Paths.get(getTempDir(), "delta-workspace-${UUID.randomUUID()}").apply {
        Files.createDirectories(this)
    }

    @Volatile
    var classpath: List<File> = emptyList()

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

    fun addPostBuildHook(hook: (internalRoot: Path) -> Unit) {
//        postBuildHooks.add(hook)
    }
}
