package suive.model

sealed class ResponseMessage(
    open val id: String // TODO this may be an integer.
) : Message() {
    data class Success<Result>(
        override val id: String,
        val result: Result?
    ) : ResponseMessage(id)

    data class Error(
        override val id: String,
        val error: ResponseError
    ) : ResponseMessage(id)
}
