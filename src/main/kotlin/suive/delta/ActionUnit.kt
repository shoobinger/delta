package suive.delta

import suive.delta.model.Params
import kotlin.reflect.KClass

class ActionUnit<P : Params, PK : KClass<out P>>(
    val methodName: String,
    val paramsClass: PK,
    val action: (Request, P) -> Unit
) {
    fun execute(request: Request, p: Params) {
        action(request, p as P) // TODO
    }
}
