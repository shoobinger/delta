package suive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import suive.method.InitializeMethod
import suive.method.Method
import suive.model.InitializeParams
import suive.model.Params
import suive.model.Result
import suive.service.DiagnosticService
import suive.task.DiagnosticsTask
import kotlin.reflect.KClass

object Application {
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

    fun dispatch(
        methodName: String,
        paramsRaw: Map<*, *>,
        handleResult: (Result) -> Unit,
        handleNotification: (String, Params) -> Unit,
        handleError: () -> Unit
    ) {
        val actionUnit = requireNotNull(dispatchTable[methodName]) { "No such method" }

        @Suppress("UNCHECKED_CAST")
        val method = actionUnit.method() as Method<Params, *>
        val params = paramsConverter.convertValue(paramsRaw, actionUnit.paramsClass.java) ?: error { "Params are null" }
        val result = method.doProcess(params)
        handleResult(result)

        actionUnit.notificationTasks().forEach { task ->
            task.execute().forEach {
                handleNotification(task.method(), it)
            }
        }
    }
}
