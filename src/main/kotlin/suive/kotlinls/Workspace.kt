package suive.kotlinls

import org.tinylog.kotlin.Logger
import suive.kotlinls.model.TextDocumentContentChangeEvent
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Workspace {
    lateinit var externalRoot: Path
    val internalRoot = Paths.get("/tmp", "kotlinls-workspace-${UUID.randomUUID()}/").apply {
        Files.createDirectories(this)
    }

    val locks = ConcurrentHashMap<Path, Any>()

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

    fun toInternalPath(fileUri: String): Path {
        return internalRoot.resolve(externalRoot.relativize(Paths.get(URI(fileUri))))
    }

    fun toExternalUri(internalPath: Path): String {
        return "file://" + externalRoot.resolve(internalRoot.relativize(internalPath))
    }

    fun updateFileContents(uri: String, contentChanges: List<TextDocumentContentChangeEvent>) {
        val internalPath = toInternalPath(uri)

        val lock = locks.getOrPut(internalPath) { Any() }
        Logger.debug("Lock: $lock")
        synchronized(lock) {
            for (change in contentChanges) {
                val oldContent = Files.readString(internalPath)
                Logger.debug { "Old file content: [$oldContent]" }
                val newContent = oldContent.replaceRange(
                    getOffset(oldContent, change.range.start.line, change.range.start.character),
                    getOffset(oldContent, change.range.end.line, change.range.end.character),
                    change.text
                )
                Logger.debug { "New file content: [$newContent]" }
                Files.writeString(internalPath, newContent)
            }
        }
    }

    private fun getOffset(text: String, row: Int, col: Int): Int {
        return text.lineSequence().take(row).fold(0) { acc, l -> acc + l.length + 1 /* 1 for newline */ } + col
    }
}
