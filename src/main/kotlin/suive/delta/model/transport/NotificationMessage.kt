package suive.delta.model.transport

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
data class NotificationMessage<P : Any>(
    override val method: String,
    override val params: P
) : Message(), WithMethod, WithParams
