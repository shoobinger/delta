package suive.delta.model.transport

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
data class RequestMessage(
    override val id: Int,
    override val method: String,
    override val params: Any?
) : Message(), WithMethod, WithId, WithParams
