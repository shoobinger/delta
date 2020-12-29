package suive.kotlinls.model

import com.fasterxml.jackson.annotation.JsonIgnore
import suive.kotlinls.method.Request

sealed class Output {
    abstract class Result(@get: JsonIgnore open val request: Request) : Output()

    data class Notification<P : Params>(
        val method: String,
        val params: P
    ) : Output()
}
