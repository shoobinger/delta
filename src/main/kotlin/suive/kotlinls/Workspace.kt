package suive.kotlinls

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.incremental.ICReporterBase
import org.jetbrains.kotlin.incremental.makeIncrementally
import org.tinylog.kotlin.Logger
import suive.kotlinls.model.Output
import suive.kotlinls.model.PublishDiagnosticsParams
import suive.kotlinls.model.TextDocumentContentChangeEvent
import suive.kotlinls.service.CompilerService
import suive.kotlinls.service.SenderService
import suive.kotlinls.util.DiagnosticMessageCollector
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
    private val senderService: SenderService,
    private val compilerService: CompilerService
) {
    companion object {
        const val DIAGNOSTIC_DELAY = 100L
    }
    lateinit var externalRoot: Path
    val internalRoot = Paths.get("/tmp", "kotlinls-workspace-${UUID.randomUUID()}").apply {
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
            diagnosticRunner.interrupt()
        }
    }

    val problematicFiles = mutableSetOf<String>()
    private val diagnosticSemaphore = Semaphore(0)
    private var lastRun = 0L
    @Volatile
    private var diagnosticInProgress = false
    private val diagnosticRunner = thread(start = true, name = "DiagnosticRunner") {
        while (true) {
            diagnosticSemaphore.acquireUninterruptibly()
            try {
                diagnosticInProgress = true
                Thread.sleep(DIAGNOSTIC_DELAY)
                Logger.debug { "Compiling started (last was ${System.currentTimeMillis() - lastRun}ms)....." }
                lastRun = System.currentTimeMillis()
                val messageCollector = DiagnosticMessageCollector(this)
                val classpath = internalRoot.resolve(Paths.get("lib", "kotlin-stdlib-1.4.21.jar"))
                    .toAbsolutePath().toString()
                val args = K2JVMCompilerArguments().apply {
                    destination =
                        internalRoot.resolve(CompilerService.CLASSES_DIR_NAME).toAbsolutePath().toString()
                    this.classpath = classpath
                    noStdlib = true
                    moduleName = "test"
                    noReflect = true
                }

                Logger.debug { "Compiler starting: ${args.destination}" }

                measureTimeMillis {
                    makeIncrementally(
                        internalRoot.resolve(CompilerService.CACHES_DIR_NAME).toFile(),
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
                    val newDiagnostics = messageCollector.diagnostics.groupBy({ it.first }, { it.second }).map { (t, u) ->
                        PublishDiagnosticsParams(t, u)
                    }
                    problematicFiles.addAll(newDiagnostics.map { it.uri })
                    newDiagnostics
                }
                diagnostics.forEach {
                    senderService.send(Output.Notification("textDocument/publishDiagnostics", it))
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
                if (diagnosticInProgress) {
                    diagnosticRunner.interrupt()
                }
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
}
