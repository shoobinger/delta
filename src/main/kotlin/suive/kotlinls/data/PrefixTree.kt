package suive.kotlinls.data

import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger

class PrefixTree {

    private data class Node(
        val id: Int,
        val char: Char,
        var isTerminal: Boolean,
        val children: Array<Node?>,
        val parent: Node? = null,
        val nextCapitals: MutableSet<Node> = HashSet()
    ) {
        override fun equals(other: Any?): Boolean {
            val o = other as? Node ?: return false
            return o.id == id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun toString(): String {
            return char.toString()
        }
    }

    private val idCounter = AtomicInteger(0)
    private val rootNode: Node = Node(idCounter.getAndIncrement(), '@', false, arrayOfNulls(255))

    fun add(vararg words: String) {
        words.forEach(::add)
    }

    fun add(word: String) {
        var currentNode = rootNode
        val capitalNodes = mutableSetOf<Node>()
        for (c in word) {
            val child = currentNode.children[c.toInt()]
            currentNode = child ?: let {
                val newNode = Node(idCounter.getAndIncrement(), c, false, arrayOfNulls(255), currentNode)
                currentNode.children[c.toInt()] = newNode
                newNode
            }
            if (c.isUpperCase()) {
                capitalNodes += currentNode
            }
        }
        currentNode.isTerminal = true

        for (capNode in capitalNodes) {
            // Go up the tree and link every node to this capital node.
            var curNode = capNode.parent
            while (curNode != null) {
                curNode.nextCapitals += capNode
                curNode = curNode.parent
            }
        }
    }

    fun query(prefix: String): List<String> {
        var currentNode = rootNode
        for (c in prefix) {
            currentNode = currentNode.children[c.toInt()] ?: return emptyList()
        }

        val result = mutableListOf<String>()
        val nodesToSearch = LinkedList<Pair<String, Node>>()
        nodesToSearch.push(prefix to currentNode)

        while (nodesToSearch.isNotEmpty()) {
            val (currentPrefix, node) = nodesToSearch.pop()
            val nextPrefix = if (node == currentNode) currentPrefix else currentPrefix + node.char
            if (node.isTerminal) {
                result.add(nextPrefix)
            }

            for (child in node.children.filterNotNull()) {
                nodesToSearch.push(nextPrefix to child)
            }
            for (nextCapital in node.nextCapitals) {
                nodesToSearch.push(nextPrefix to nextCapital)
            }
        }

        return result
    }

    fun delete(w: String) {

    }
}
