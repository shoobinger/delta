package suive.delta.model.transport


sealed class ResponseMessage(
    open val id: Int // TODO this may be a string per spec.
) : Message() {
    data class Success(
        override val id: Int,
        val result: Any?
    ) : ResponseMessage(id)

    data class Error(
        override val id: Int,
        val error: ResponseError
    ) : ResponseMessage(id)
}
