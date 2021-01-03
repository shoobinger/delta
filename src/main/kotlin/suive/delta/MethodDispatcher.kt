package suive.delta

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import suive.delta.model.DidChangeTextDocumentParams
import suive.delta.model.InitializeParams
import suive.delta.model.NoParams
import suive.delta.service.MavenClasspathCollector
import suive.delta.service.SenderService
import suive.delta.service.TaskService
import suive.delta.service.WorkspaceService
import suive.delta.util.NamedThreadFactory
import java.io.OutputStream
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class MethodDispatcher(
    outputStream: OutputStream,
    jsonConverter: ObjectMapper
) {
    private val mavenClasspathCollector = MavenClasspathCollector()
    private val senderService = SenderService(outputStream, jsonConverter)
    private val workspace = Workspace(senderService)
    private val taskService = TaskService()
    private val workspaceService = WorkspaceService(mavenClasspathCollector, workspace, taskService, senderService)

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
            action = { r, p ->
                workspaceService.syncDocumentChanges(r, p)
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
        val actionUnit = requireNotNull(dispatchTable[methodName]) { "Server does not support $methodName" }

        workerThreadPool.execute {
            actionUnit.performAction(request, paramsRaw ?: NoParams, paramsConverter)
        }
    }
}
