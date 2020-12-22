package suive.model.transport

data class RequestMessage(
    val id: String,
    val method: String,
    val params: Map<*, *>
) : Message()
