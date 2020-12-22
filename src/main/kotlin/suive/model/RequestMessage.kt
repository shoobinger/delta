package suive.model

data class RequestMessage(
    val id: String,
    val method: String,
    val params: Map<*, *>
) : Message()
