package suive.delta.model.transport

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
sealed class ResponseMessage(
    override val id: Int // TODO this may be a string per spec.
) : Message(), WithId {
    data class Success(
        override val id: Int,
        val result: Any?
    ) : ResponseMessage(id)

    data class Error(
        override val id: Int,
        val error: ResponseError
    ) : ResponseMessage(id)
}
