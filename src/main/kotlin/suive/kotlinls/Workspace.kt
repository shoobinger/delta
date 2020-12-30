package suive.kotlinls

import com.google.common.jimfs.Jimfs
import org.jetbrains.kotlin.konan.file.use
import org.tinylog.kotlin.Logger
import suive.kotlinls.model.TextDocumentContentChangeEvent
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

class Workspace() {
    lateinit var externalRoot: Path
    val internalRoot = Paths.get("/tmp", "kotlinls-workspace-${UUID.randomUUID()}/").apply {
        Files.createDirectories(this)
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

    fun toInternalPath(fileUri: String): Path {
        return internalRoot.resolve(externalRoot.relativize(Paths.get(URI(fileUri))))
    }

    fun toExternalUri(internalPath: Path): String {
        return "file://" + externalRoot.resolve(internalRoot.relativize(internalPath))
    }

    fun updateFileContents(uri: String, contentChanges: List<TextDocumentContentChangeEvent>) {
        val internalPath = toInternalPath(uri)

        for (change in contentChanges) {
            val start = change.range.start
            val end = change.range.end
            Logger.debug { "Old file content: ${internalPath.toFile().readText()}" }
            val newLines = Files.lines(internalPath).use { lines ->
                lines.iterator().asSequence().foldIndexed(mutableListOf<String>()) { i, acc, l ->
                    val newLine = if (i < start.line || i > end.line) {
                        l
                    } else {
                        val from = if (i == start.line)
                            start.character
                        else 0
                        val to = if (i == end.line)
                            end.character
                        else l.length - 1

                        l.replaceRange(from, to, change.text.lines()[i - start.line])
                    }

                    acc.add(newLine)
                    acc
                }
            }
            Files.write(internalPath, newLines)
            Logger.debug { "New file content: ${internalPath.toFile().readText()}" }
        }
    }
}
