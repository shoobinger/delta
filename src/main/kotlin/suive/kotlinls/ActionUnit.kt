package suive.kotlinls

import suive.kotlinls.method.Method
import suive.kotlinls.model.Params
import suive.kotlinls.task.Task
import kotlin.reflect.KClass

class ActionUnit<P : Params, K : Method<P, *>, PK : KClass<out P>>(
    val methodName: String,
    val paramsClass: PK,
    val method: () -> K,
    val tasks: () -> List<Task<*>>
)
