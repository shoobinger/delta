package suive.model

data class NotificationMessage<P : Params>(
    val method: String,
    val params: P
) : Message()
