package suive.delta.service

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.incremental.ICReporterBase
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.makeIncrementally
import org.tinylog.kotlin.Logger
import suive.delta.Workspace
import suive.delta.executeTimed
import suive.delta.model.PublishDiagnosticsParams
import suive.delta.model.transport.NotificationMessage
import java.io.File
import java.nio.file.Files
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class Builder(
    private val workspace: Workspace,
    private val sender: Sender
) {
    private val buildRequestQueue = LinkedBlockingQueue<BuildRequest>()

    private val problematicFiles = mutableSetOf<String>()

    private var buildInProgress = AtomicBoolean(false)

    private val messageCollector = DiagnosticMessageCollector(workspace)

    private val buildRunner = thread(start = true, name = "BuildRunner") {
        while (!Thread.interrupted()) {
            try {
                val buildRequest = buildRequestQueue.take()
                Logger.info { "Build request: $buildRequest" }
                buildInProgress.set(true)
                Thread.sleep(buildRequest.buildDelay)

                val cacheDir = workspace.internalRoot.resolve("cache")
                val srcDir = workspace.internalRoot.resolve("src")
                val classesDir = workspace.internalRoot.resolve("classes")

                if (buildRequest.cleanBuild) {
                    Files.deleteIfExists(cacheDir.resolve("build-history.bin"))
                    Files.deleteIfExists(cacheDir.resolve("last-build.bin"))
                }

                val args = K2JVMCompilerArguments().apply {
                    destination = classesDir.toAbsolutePath().toString()
                    classpathAsList = workspace.classpath
                    noStdlib = true
                    moduleName = "test"
                    noReflect = true
                    jvmTarget = "1.8"
                }

                Logger.debug { "Compiler starting. classpath: ${args.classpath}" }

                executeTimed("make incrementally") {
                    makeIncrementally(
                        cacheDir.toFile(),
                        listOf(srcDir.toFile()),
                        args,
                        messageCollector,
                        ICReporter
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
                    sender.send(NotificationMessage("textDocument/publishDiagnostics", it))
                }
            } catch (e: Exception) {
                Logger.debug(e) { "Compilation cancelled" }
            } finally {
                buildInProgress.set(false)
            }
        }
    }

    fun enqueueBuild(buildRequest: BuildRequest) {
        buildRequestQueue.clear()
        buildRequestQueue.offer(buildRequest)
    }

    fun cancelBuild() {
        if (buildInProgress.get()) {
            Logger.info { "Attempting to interrupt running build" }
            buildRunner.interrupt()
        }
    }

    object ICReporter : ICReporterBase() {
        override fun report(message: () -> String) {
            Logger.debug(message)
        }

        override fun reportCompileIteration(
            incremental: Boolean,
            sourceFiles: Collection<File>,
            exitCode: ExitCode
        ) {
            Logger.debug { "Incremental: $incremental, source files $sourceFiles, exit code $exitCode" }
        }

        override fun reportVerbose(message: () -> String) {
            Logger.debug(message)
        }
    }
}
