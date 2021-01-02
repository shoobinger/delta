package suive.delta.model.transport

import suive.delta.model.Output

sealed class ResponseMessage(
    open val id: Int // TODO this may be a string per spec.
) : Message() {
    data class Success<R : Output.Result>(
        override val id: Int,
        val result: R?
    ) : ResponseMessage(id)

    data class Error(
        override val id: Int,
        val error: ResponseError
    ) : ResponseMessage(id)
}
