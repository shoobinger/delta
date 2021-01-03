package suive.delta.service

import org.tinylog.kotlin.Logger
import suive.delta.Request
import suive.delta.Workspace
import suive.delta.model.DidChangeTextDocumentParams
import suive.delta.model.DidChangeWatchedFilesParams
import suive.delta.model.DidChangeWatchedFilesRegistrationOptions
import suive.delta.model.FileSystemWatcher
import suive.delta.model.InitializeParams
import suive.delta.model.InitializeResult
import suive.delta.model.Registration
import suive.delta.model.RegistrationParams
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

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
            Logger.info { "Registering for project descriptor updates" }
            senderService.sendRequest(
                "client/registerCapability", RegistrationParams(
                    listOf(
                        Registration(
                            id = UUID.randomUUID().toString(),
                            method = "workspace/didChangeWatchedFiles",
                            registerOptions = DidChangeWatchedFilesRegistrationOptions(
                                listOf(
                                    FileSystemWatcher("**/pom.xml")
                                )
                            )
                        )
                    )
                )
            )

            val pom = workspace.externalRoot.resolve("pom.xml")
            val classpath = if (Files.exists(pom)) {
                Logger.info { "Found pom.xml, resolving classpath" }
                classpathCollector.collect(pom)
            } else {
                emptyList()
            }
            workspace.updateClasspath(classpath)
        }

        senderService.sendResponse(request.requestId, InitializeResult())
    }

    fun syncDocumentChanges(request: Request, params: DidChangeTextDocumentParams) {
        params.contentChanges.forEach { change ->
            workspace.enqueueChange(params.textDocument.uri, change)
        }
    }

    fun handleWatchedFileChange(request: Request, params: DidChangeWatchedFilesParams) {
        params.changes.forEach { event ->
            val file = Paths.get(URI(event.uri))
            if (file.fileName.toString() == "pom.xml") {
                workspace.updateClasspath(classpathCollector.collect(file))
            }
        }
    }
}
