package suive.delta

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.tinylog.kotlin.Logger
import suive.delta.method.InitializeMethod
import suive.delta.method.Method
import suive.delta.method.NoOpMethod
import suive.delta.method.Request
import suive.delta.model.DidChangeTextDocumentParams
import suive.delta.model.InitializeParams
import suive.delta.model.NoParams
import suive.delta.model.Output
import suive.delta.model.Params
import suive.delta.service.MavenClasspathCollector
import suive.delta.service.SenderService
import suive.delta.task.DocumentSyncTask
import suive.delta.task.NotificationTask
import suive.delta.task.UpdateClasspathTask
import suive.delta.util.WorkerThreadFactory
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

    private val paramsConverter = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val actionUnits = listOf<ActionUnit<*, out Method<*, *>, out KClass<out Params>>>(
        ActionUnit(
            methodName = "initialize",
            paramsClass = InitializeParams::class,
            method = { InitializeMethod(workspace) },
            tasks = { params ->
                listOf(
                    UpdateClasspathTask(mavenClasspathCollector, workspace),
//                    IndexingTask(indexingService)
                )
            }
        ),
        ActionUnit(
            methodName = "shutdown",
            paramsClass = NoParams::class,
            method = { NoOpMethod() },
        ),
//        ActionUnit(
//            methodName = "textDocument/completion",
//            paramsClass = CompletionParams::class,
//            method = { CompletionMethod(completionService) }
//        ),
        ActionUnit(
            methodName = "textDocument/didChange",
            paramsClass = DidChangeTextDocumentParams::class,
            tasks = { params ->
                listOf(
                    DocumentSyncTask(workspace, params)
                )
            }
        )
    )

    private val dispatchTable = actionUnits.associateBy { it.methodName }

    private val workerThreadPool = Executors.newCachedThreadPool(WorkerThreadFactory())

    fun dispatch(
        request: Request,
        methodName: String,
        paramsRaw: Map<*, *>?
    ) {
        val actionUnit = requireNotNull(dispatchTable[methodName]) { "Server does not support $methodName" }

        val method = actionUnit.method

        val params = if (paramsRaw == null)
            NoParams
        else
            paramsConverter.convertValue(paramsRaw, actionUnit.paramsClass.java) ?: error { "Params are null" }
        workerThreadPool.execute {
            if (method != null) {
                Logger.info { "Executing $method" }
                @Suppress("UNCHECKED_CAST")
                val result = (method() as Method<Params, *>).doProcess(request, params)
                senderService.send(result)
            }

            actionUnit.getTasks(params).forEach { task ->
                Logger.debug { "Executing $task." }
                if (task is NotificationTask<*>) {
                    task.execute().map { Output.Notification(task.method(), it) }.forEach {
                        senderService.send(it)
                    }
                } else {
                    task.execute()
                }
            }
        }
    }
}
