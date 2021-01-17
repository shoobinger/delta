package suive.delta.service

import org.tinylog.kotlin.Logger
import suive.delta.Request
import suive.delta.Workspace
import suive.delta.model.CompletionRegistrationOptions
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
    private val helper: MavenHelper,
    private val workspace: Workspace,
    private val taskService: TaskService,
    private val senderService: SenderService,
    private val globalSearchService: GlobalSearchService
) {
    fun initialize(request: Request, params: InitializeParams) {
        if (params.rootUri != null) {
            Logger.info { "Initializing workspace ${params.rootUri}" }
            workspace.initialize(Paths.get(URI(params.rootUri)))
        }

        taskService.execute {
            Logger.info { "Registering dynamic client capabilities" }
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
                        ),
                        Registration(
                            id = UUID.randomUUID().toString(),
                            method = "textDocument/completion",
                            registerOptions = CompletionRegistrationOptions()
                        )
                    )
                )
            )

            val pom = workspace.externalRoot.resolve("pom.xml")
            val classpath = if (Files.exists(pom)) {
                Logger.info { "Found pom.xml, resolving classpath" }
                helper.collectDependencies(pom)
            } else {
                emptyList()
            }
            workspace.updateClasspath(classpath)
            globalSearchService.indexClasses()
        }

        senderService.sendResponse(request.requestId, InitializeResult())
    }

    fun syncDocumentChanges(params: DidChangeTextDocumentParams) {
        params.contentChanges.forEach { change ->
            workspace.enqueueChange(params.textDocument.uri, change)
        }
    }

    fun handleWatchedFileChange(params: DidChangeWatchedFilesParams) {
        params.changes.forEach { event ->
            val file = Paths.get(URI(event.uri))
            if (file.fileName.toString() == "pom.xml") {
                workspace.updateClasspath(helper.collectDependencies(file))
            }
        }
    }
}
