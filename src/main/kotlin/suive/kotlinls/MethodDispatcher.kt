package suive.kotlinls

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.tinylog.kotlin.Logger
import suive.kotlinls.method.CompletionMethod
import suive.kotlinls.method.InitializeMethod
import suive.kotlinls.method.Method
import suive.kotlinls.method.NoOpMethod
import suive.kotlinls.method.Request
import suive.kotlinls.model.CompletionParams
import suive.kotlinls.model.DidChangeTextDocumentParams
import suive.kotlinls.model.InitializeParams
import suive.kotlinls.model.NoParams
import suive.kotlinls.model.Output
import suive.kotlinls.model.Params
import suive.kotlinls.service.CompilerService
import suive.kotlinls.service.CompletionService
import suive.kotlinls.service.DiagnosticService
import suive.kotlinls.service.MavenClasspathCollector
import suive.kotlinls.service.SenderService
import suive.kotlinls.service.SymbolSearchIndexingService
import suive.kotlinls.task.DiagnosticsTask
import suive.kotlinls.task.DocumentSyncTask
import suive.kotlinls.task.IndexingTask
import suive.kotlinls.task.NotificationTask
import suive.kotlinls.task.UpdateClasspathTask
import suive.kotlinls.util.WorkerThreadFactory
import java.io.OutputStream
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class MethodDispatcher(
    outputStream: OutputStream,
    jsonConverter: ObjectMapper
) {
    private val indexingService = SymbolSearchIndexingService()
    private val mavenClasspathCollector = MavenClasspathCollector()
    private val compilerService = CompilerService()
    private val diagnosticService = DiagnosticService(compilerService)
    private val completionService = CompletionService(compilerService)
    private val senderService = SenderService(outputStream, jsonConverter)
    private val workspace = Workspace(senderService, compilerService)

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
                    UpdateClasspathTask(mavenClasspathCollector, compilerService),
                    DiagnosticsTask(compilerService, workspace, requireNotNull(params.rootUri)),
                    IndexingTask(indexingService)
                )
            }
        ),
        ActionUnit(
            methodName = "shutdown",
            paramsClass = NoParams::class,
            method = { NoOpMethod() },
        ),
        ActionUnit(
            methodName = "textDocument/completion",
            paramsClass = CompletionParams::class,
            method = { CompletionMethod(completionService) }
        ),
        ActionUnit(
            methodName = "textDocument/didChange",
            paramsClass = DidChangeTextDocumentParams::class,
            tasks = { params ->
                listOf(
                    DocumentSyncTask(workspace, params),
//                    DiagnosticsTask(compilerService, workspace, workspace.externalRoot.toUri().toString())
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
        val actionUnit = requireNotNull(dispatchTable[methodName]) { "No such method" }

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
