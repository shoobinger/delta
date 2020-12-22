package suive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import suive.method.InitializeMethod
import suive.method.Method
import suive.method.Request
import suive.model.InitializeParams
import suive.model.Output
import suive.model.Params
import suive.service.DiagnosticService
import suive.task.DiagnosticsTask
import suive.util.NamedThreadFactory
import java.util.concurrent.Executors
import kotlin.reflect.KClass

object MethodDispatcher {
    private val diagnosticService = DiagnosticService()
    private val paramsConverter = ObjectMapper().registerModule(KotlinModule())

    private val actionUnits = listOf<ActionUnit<out Params, out Method<*, *>, out KClass<out Params>>>(
        ActionUnit(
            methodName = "initialize",
            paramsClass = InitializeParams::class,
            method = { InitializeMethod() },
            notificationTasks = { listOf(DiagnosticsTask(diagnosticService)) }
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

            actionUnit.notificationTasks().forEach { task ->
                task.execute().map { Output.Notification(task.method(), it) }.forEach {
                    yield(it)
                }
            }
        }
    }
}
