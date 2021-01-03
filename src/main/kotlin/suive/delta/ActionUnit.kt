package suive.delta

import com.fasterxml.jackson.databind.ObjectMapper
import suive.delta.model.Params
import kotlin.reflect.KClass

class ActionUnit<P : Params, PK : KClass<out P>>(
    val methodName: String,
    val paramsClass: PK,
    val action: (Request, P) -> Unit
) {
    fun performAction(request: Request, p: Map<*, *>?, paramsConverter: ObjectMapper) {
        val params = paramsConverter.convertValue(p, paramsClass.java) ?: error { "Params are null" }
        action(request, params)
    }
}
