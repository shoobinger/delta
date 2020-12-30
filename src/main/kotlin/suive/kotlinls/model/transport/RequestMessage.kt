package suive.kotlinls.model.transport

data class RequestMessage(
    val id: Int?,
    val method: String,
    val params: Map<*, *>?
) : Message()
