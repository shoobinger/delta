package suive.kotlinls

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import suive.kotlinls.method.InitializeMethod
import suive.kotlinls.method.Method
import suive.kotlinls.method.Request
import suive.kotlinls.model.InitializeParams
import suive.kotlinls.model.Output
import suive.kotlinls.model.Params
import suive.kotlinls.service.DiagnosticService
import suive.kotlinls.service.SimpleIndexingService
import suive.kotlinls.task.DiagnosticsTask
import suive.kotlinls.task.IndexingTask
import suive.kotlinls.task.NotificationTask
import suive.kotlinls.util.NamedThreadFactory
import java.util.concurrent.Executors
import kotlin.reflect.KClass

object MethodDispatcher {
    private val diagnosticService = DiagnosticService()
    private val indexingService = SimpleIndexingService()
    private val paramsConverter = ObjectMapper().registerModule(KotlinModule())

    private val actionUnits = listOf<ActionUnit<out Params, out Method<*, *>, out KClass<out Params>>>(
        ActionUnit(
            methodName = "initialize",
            paramsClass = InitializeParams::class,
            method = { InitializeMethod() },
            tasks = { listOf(DiagnosticsTask(diagnosticService), IndexingTask(indexingService)) }
        )
    )

    private val dispatchTable = actionUnits.associateBy { it.methodName }

    private val workerThreadPool = Executors.newCachedThreadPool(NamedThreadFactory("Worker"))

    fun dispatch(
        request: Request,
        methodName: String,
        paramsRaw: Map<*, *>,
        yield: (Output) -> Unit
    ) {
        val actionUnit = requireNotNull(dispatchTable[methodName]) { "No such method" }

        @Suppress("UNCHECKED_CAST")
        val method = actionUnit.method() as Method<Params, *>
        val params = paramsConverter.convertValue(paramsRaw, actionUnit.paramsClass.java) ?: error { "Params are null" }
        workerThreadPool.execute {
            val result = method.doProcess(request, params)
            yield(result)

            actionUnit.tasks().forEach { task ->
                if (task is NotificationTask<*>) {
                    task.execute().map { Output.Notification(task.method(), it) }.forEach {
                        yield(it)
                    }
                } else {
                    task.execute()
                }
            }
        }
    }
}
