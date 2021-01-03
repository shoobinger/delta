package suive.delta.model.transport

data class NotificationMessage<P: Any>(
    val method: String,
    val params: P
) : Message()
