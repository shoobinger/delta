package suive.method

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

abstract class Method<in Params : Any, out Result : Any>(
    private val paramsClass: KClass<Params>,
    private val objectMapper: ObjectMapper
) {

    fun process(params: Any?): Any {
        return doProcess(objectMapper.convertValue(params, paramsClass.java))
    }

    protected abstract fun doProcess(params: Params): Result
}
