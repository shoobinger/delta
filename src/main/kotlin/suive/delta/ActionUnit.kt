package suive.delta

import com.fasterxml.jackson.databind.ObjectMapper
import suive.delta.util.InvalidParamsException
import kotlin.reflect.KClass

class ActionUnit<P : Any, PK : KClass<out P>>(
    val methodName: String,
    val paramsClass: PK,
    val action: (Request, P) -> Unit
) {
    fun performAction(request: Request, p: Any?, paramsConverter: ObjectMapper) {
        val params = try {
            requireNotNull(paramsConverter.convertValue(p, paramsClass.java))
        } catch (e: IllegalArgumentException) {
            throw InvalidParamsException(e.message, e)
        }
        action(request, params)
    }
}
