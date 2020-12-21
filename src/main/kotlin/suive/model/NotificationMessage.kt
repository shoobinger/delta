package suive.model

data class NotificationMessage<Params>(
    val method: String,
    val params: Params
) : Message()
