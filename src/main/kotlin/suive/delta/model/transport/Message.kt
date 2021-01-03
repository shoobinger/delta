package suive.delta.model.transport

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import suive.delta.util.MessageDeserializer

@JsonDeserialize(using = MessageDeserializer::class)
abstract class Message {
    val jsonrpc = "2.0"
}
