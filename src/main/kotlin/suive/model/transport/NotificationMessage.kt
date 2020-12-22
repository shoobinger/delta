package suive.model.transport

import com.fasterxml.jackson.annotation.JsonUnwrapped
import suive.model.Output
import suive.model.Params

data class NotificationMessage<P : Params>(
    @get: JsonUnwrapped
    val notification: Output.Notification<P>
) : Message()
