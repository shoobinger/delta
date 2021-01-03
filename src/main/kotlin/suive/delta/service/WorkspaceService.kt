package suive.delta.service

import org.tinylog.kotlin.Logger
import suive.delta.Workspace
import suive.delta.Request
import suive.delta.model.DidChangeTextDocumentParams
import suive.delta.model.InitializeParams
import suive.delta.model.InitializeResult
import suive.delta.model.ServerCapabilities
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

class WorkspaceService(
    private val classpathCollector: MavenClasspathCollector,
    private val workspace: Workspace,
    private val taskService: TaskService,
    private val senderService: SenderService
) {
    fun initialize(request: Request, params: InitializeParams) {
        if (params.rootUri != null) {
            Logger.info { "Initializing workspace ${params.rootUri}" }
            workspace.initialize(Paths.get(URI(params.rootUri)))
        }

        taskService.execute {
            val pom = workspace.internalRoot.resolve("pom.xml")
            val classpath = if (Files.notExists(pom)) {
                emptyList()
            } else {
                Logger.info { "Found pom.xml, resolving classpath" }
                classpathCollector.collect(pom)
            }
            workspace.updateClasspath(classpath)
        }

        senderService.send(InitializeResult(request, ServerCapabilities()))
    }

    fun syncDocumentChanges(request: Request, params: DidChangeTextDocumentParams) {
        params.contentChanges.forEach { change ->
            workspace.enqueueChange(params.textDocument.uri, change)
        }
    }
}
