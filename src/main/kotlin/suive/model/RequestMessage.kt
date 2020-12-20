package suive.model

data class RequestMessage<Params>(
    val id: String,
    val method: String,
    val params: Params
) : Message()
