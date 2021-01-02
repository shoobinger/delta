package suive.delta

import suive.delta.method.Method
import suive.delta.model.Params
import suive.delta.task.Task
import kotlin.reflect.KClass

class ActionUnit<P : Params, K : Method<P, *>, PK : KClass<out P>>(
    val methodName: String,
    val paramsClass: PK,
    val method: (() -> K)? = null,
    val tasks: (P) -> List<Task<*>> = { emptyList() }
) {
    fun getTasks(p: Any): List<Task<*>> {
        return tasks(p as P) // TODO
    }
}
