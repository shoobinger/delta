package suive.delta.util;

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import suive.delta.model.transport.Message
import suive.delta.model.transport.NotificationMessage
import suive.delta.model.transport.RequestMessage
import suive.delta.model.transport.ResponseMessage

class MessageDeserializer : StdDeserializer<Message>(Message::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Message {
        val root = p.readValueAsTree<TreeNode>()
        if (!root.isObject) {
            throw InvalidRequestException("Root node is not an object.")
        }

        val fieldNames = root.fieldNames().asSequence().toSet()
        return when {
            fieldNames.containsAll(listOf("id", "method")) -> p.codec.treeToValue(root, RequestMessage::class.java)
            fieldNames.contains("id") -> p.codec.treeToValue(root, ResponseMessage.Success::class.java)
            fieldNames.contains("method") -> p.codec.treeToValue(root, NotificationMessage::class.java)
            else -> throw InvalidRequestException("Can't deserialize message")
        };
    }
}
