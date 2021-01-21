package suive.delta

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.tinylog.kotlin.Logger
import suive.delta.model.CompletionParams
import suive.delta.model.DidChangeTextDocumentParams
import suive.delta.model.DidChangeWatchedFilesParams
import suive.delta.model.InitializeParams
import suive.delta.model.InternalError
import suive.delta.model.InvalidParams
import suive.delta.model.MethodNotFound
import suive.delta.model.NoParams
import suive.delta.model.transport.ResponseError
import suive.delta.model.transport.ResponseMessage
import suive.delta.service.Builder
import suive.delta.service.CompletionService
import suive.delta.service.Editor
import suive.delta.service.MavenHelper
import suive.delta.service.Sender
import suive.delta.service.SymbolRepository
import suive.delta.service.TaskService
import suive.delta.service.WorkspaceService
import suive.delta.util.InvalidParamsException
import suive.delta.util.NamedThreadFactory
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class Dispatcher(private val sender: Sender) {
    private val mavenClasspathCollector = MavenHelper()
    private val workspace = Workspace()
    private val builder = Builder(workspace, sender)
    private val editor = Editor(builder)
    private val taskService = TaskService()
    private val globalSearchService = SymbolRepository()
    private val workspaceService = WorkspaceService(
        mavenClasspathCollector,
        workspace,
        taskService,
        sender,
        globalSearchService,
        builder,
        editor
    )
    private val completionService = CompletionService(workspace, sender, globalSearchService)

    private val paramsConverter = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val actionUnits = listOf<ActionUnit<*, out KClass<out Any>>>(
        ActionUnit(
            methodName = "initialize",
            paramsClass = InitializeParams::class,
            action = { r, p ->
                workspaceService.initialize(r, p)
            }
        ),
        ActionUnit(
            methodName = "shutdown",
            paramsClass = NoParams::class,
            action = { _, _ -> },
        ),
        ActionUnit(
            methodName = "textDocument/didChange",
            paramsClass = DidChangeTextDocumentParams::class,
            action = { _, p ->
                workspaceService.syncDocumentChanges(p)
            }
        ),
        ActionUnit(
            methodName = "workspace/didChangeWatchedFiles",
            paramsClass = DidChangeWatchedFilesParams::class,
            action = { _, p ->
                workspaceService.handleWatchedFileChange(p)
            }
        ),
        ActionUnit(
            methodName = "textDocument/completion",
            paramsClass = CompletionParams::class,
            action = { r, p ->
                completionService.sendCompletions(
                    request = r,
                    fileUri = p.textDocument.uri,
                    row = p.position.line,
                    col = p.position.character,
                    partialResultToken = p.partialResultToken
                )
            }
        )
    )

    private val dispatchTable = actionUnits.associateBy { it.methodName }

    private val workerThreadPool = Executors.newCachedThreadPool(NamedThreadFactory("Worker-"))

    fun dispatch(
        request: Request,
        methodName: String,
        paramsRaw: Any?
    ) {
        val actionUnit = dispatchTable[methodName]

        if (actionUnit == null) {
            Logger.error { "Method not found: $methodName" }
            sender.send(
                ResponseMessage.Error(
                    request.requestId,
                    ResponseError(MethodNotFound, methodName)
                )
            )
            return
        }

        workerThreadPool.execute {
            try {
                actionUnit.performAction(request, paramsRaw ?: NoParams, paramsConverter)
            } catch (e: InvalidParamsException) {
                Logger.error(e) { "Invalid params for method $methodName" }
                sender.send(
                    ResponseMessage.Error(
                        request.requestId,
                        ResponseError(InvalidParams, e.message ?: "N/A")
                    )
                )
            } catch (e: Exception) {
                Logger.error(e) { "Action unit execution failed" }
                sender.send(
                    ResponseMessage.Error(
                        request.requestId,
                        ResponseError(InternalError, e.message ?: "N/A")
                    )
                )
            }
        }
    }
}
