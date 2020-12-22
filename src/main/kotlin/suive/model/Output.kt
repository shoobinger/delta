package suive.model

import suive.method.Request

sealed class Output {
    abstract class Result(open val request: Request) : Output()

    data class Notification<P : Params>(
        val method: String,
        val params: P
    ) : Output()
}
