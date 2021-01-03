package suive.delta.model.transport

data class RequestMessage(
    val id: Int?,
    val method: String,
    val params: Any?
) : Message()
