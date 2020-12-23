package suive.kotlinls.model.transport

import com.fasterxml.jackson.annotation.JsonUnwrapped
import suive.kotlinls.model.Output
import suive.kotlinls.model.Params

data class NotificationMessage<P : Params>(
    @get: JsonUnwrapped
    val notification: Output.Notification<P>
) : Message()
