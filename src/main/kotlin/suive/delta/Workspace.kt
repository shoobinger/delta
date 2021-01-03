package suive.delta

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.incremental.ICReporterBase
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.makeIncrementally
import org.tinylog.kotlin.Logger
import suive.delta.model.PublishDiagnosticsParams
import suive.delta.model.TextDocumentContentChangeEvent
import suive.delta.model.transport.NotificationMessage
import suive.delta.service.SenderService
import suive.delta.util.DiagnosticMessageCollector
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class Workspace(
    private val senderService: SenderService
) {
    companion object {
        const val DIAGNOSTIC_DELAY = 100L
    }

    lateinit var externalRoot: Path
    val internalRoot = Paths.get("/tmp", "delta-workspace-${UUID.randomUUID()}").apply {
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

    fun toInternalPath(fileUri: String): Path {
        return internalRoot.resolve(externalRoot.relativize(Paths.get(URI(fileUri))))
    }

    fun toExternalUri(internalPath: Path): String {
        return "file://" + externalRoot.resolve(internalRoot.relativize(internalPath))
    }

    private data class DocumentEdit(val uri: String, val change: TextDocumentContentChangeEvent)

    val icReporter = object : ICReporterBase() {
        override fun report(message: () -> String) {
            Logger.tag("Kotlin Compiler").debug(message)
        }

        override fun reportCompileIteration(
            incremental: Boolean,
            sourceFiles: Collection<File>,
            exitCode: ExitCode
        ) {
            Logger.tag("Kotlin Compiler")
                .debug { "Inc $incremental, source files $sourceFiles, exit code $exitCode" }
        }

        override fun reportVerbose(message: () -> String) {
            Logger.tag("Kotlin Compiler").debug(message)
        }
    }

    fun startDiagnostics() {
        diagnosticSemaphore.release()
    }

    fun cancelDiagnostics() {
        if (diagnosticInProgress) {
            Logger.info { "Attempting to interrupt running diagnostics" }
            diagnosticRunner.interrupt()
        }
    }

    private val problematicFiles = mutableSetOf<String>()
    private val diagnosticSemaphore = Semaphore(0)

    @Volatile
    private var diagnosticInProgress = false
    private val diagnosticRunner = thread(start = true, name = "DiagnosticRunner") {
        while (true) {
            diagnosticSemaphore.acquireUninterruptibly()
            try {
                diagnosticInProgress = true
                Thread.sleep(DIAGNOSTIC_DELAY)
                val messageCollector = DiagnosticMessageCollector(this)
                val args = K2JVMCompilerArguments().apply {
                    destination =
                        internalRoot.resolve("classes").toAbsolutePath().toString()
                    classpathAsList = this@Workspace.classpath
                    noStdlib = true
                    moduleName = "test"
                    noReflect = true
                    jvmTarget = "1.8"
                }

                Logger.debug { "Compiler starting. classpath: ${args.classpath}" }

                measureTimeMillis {
                    makeIncrementally(
                        internalRoot.resolve("cache").toFile(),
                        listOf(internalRoot.resolve("src").toFile()),
                        args,
                        messageCollector,
                        icReporter
                    )
                }
                val diagnostics = if (messageCollector.diagnostics.isEmpty()) {
                    // Compilation finished without errors.
                    // Send empty diagnostics with previous files to clear messages in the client.
                    problematicFiles.map { uri -> PublishDiagnosticsParams(uri, emptyList()) }.also {
                        problematicFiles.clear()
                    }
                } else {
                    val newDiagnostics =
                        messageCollector.diagnostics.groupBy({ it.first }, { it.second }).map { (t, u) ->
                            PublishDiagnosticsParams(t, u)
                        }
                    problematicFiles.addAll(newDiagnostics.map { it.uri })
                    newDiagnostics
                }
                diagnostics.forEach {
                    senderService.send(NotificationMessage("textDocument/publishDiagnostics", it))
                }
            } catch (e: Exception) {
                Logger.debug(e) { "Compilation cancelled" }
            } finally {
                diagnosticInProgress = false
            }
        }
    }

    private val editQueue = LinkedBlockingQueue<DocumentEdit>()
    val editorThread = thread(start = true, name = "Editor") {
        while (!Thread.interrupted()) {
            try {
                val (uri, change) = editQueue.take()
                cancelDiagnostics()
                updateFileContents(uri, change)
                startDiagnostics()
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun updateFileContents(uri: String, change: TextDocumentContentChangeEvent) {
        val internalPath = toInternalPath(uri)

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

    private fun getOffset(text: String, row: Int, col: Int): Int {
        return text.lineSequence().take(row).fold(0) { acc, l -> acc + l.length + 1 /* 1 for newline */ } + col
    }

    fun enqueueChange(uri: String, change: TextDocumentContentChangeEvent) {
        editQueue.offer(DocumentEdit(uri, change))
    }

    fun updateClasspath(paths: List<Path>) {
        classpath = paths.map { it.toFile() }
        Logger.info { "Classpath updated, new classpath $classpath" }
        startDiagnostics()
    }
}
