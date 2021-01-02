package suive.delta.model.transport

import com.fasterxml.jackson.annotation.JsonUnwrapped
import suive.delta.model.Output
import suive.delta.model.Params

data class NotificationMessage<P : Params>(
    @get: JsonUnwrapped
    val notification: Output.Notification<P>
) : Message()
