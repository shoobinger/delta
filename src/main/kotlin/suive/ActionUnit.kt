package suive

import suive.method.Method
import suive.model.Params
import suive.task.Task
import kotlin.reflect.KClass

class ActionUnit<P : Params, K : Method<P, *>, PK : KClass<out P>>(
    val methodName: String,
    val paramsClass: PK,
    val method: () -> K,
    val notificationTasks: () -> List<Task<*>>
)
