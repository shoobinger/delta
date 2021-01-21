package suive.delta.service

import org.tinylog.kotlin.Logger
import suive.delta.model.TextDocumentContentChangeEvent
import suive.delta.util.getOffset
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class Editor(
    private val builder: Builder
) {

    companion object {
        private const val EDITOR_BUILD_DELAY = 100L
    }

    private data class DocumentEdit(val internalPath: Path, val change: TextDocumentContentChangeEvent)

    private val editQueue = LinkedBlockingQueue<DocumentEdit>()
    private val editorThread = thread(start = true, name = "Editor") {
        while (!Thread.interrupted()) {
            try {
                val (internalPath, change) = editQueue.take()
                builder.cancelBuild()
                updateFileContents(internalPath, change)
                builder.enqueueBuild(BuildRequest(buildDelay = EDITOR_BUILD_DELAY))
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun enqueueEdit(internalPath: Path, change: TextDocumentContentChangeEvent) {
        editQueue.offer(DocumentEdit(internalPath, change))
    }

    private fun updateFileContents(internalPath: Path, change: TextDocumentContentChangeEvent) {
        val oldContent = Files.readString(internalPath)
        Logger.debug { "Old file content: [$oldContent]" }
        val newContent = oldContent.replaceRange(
            getOffset(oldContent, change.range.start.line, change.range.start.character),
            getOffset(oldContent, change.range.end.line, change.range.end.character),
            change.text
        )
        Logger.debug { "New file content: [$newContent]" }
        internalPath.toFile().writeText(newContent)
    }
}
