package suive.delta.data

import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger

class SymbolSearchGraph {

    private data class Node(
        val id: Int,
        val char: Char,
        var isTerminal: Boolean,
        val parent: Node? = null,
        val next: MutableMap<Char, MutableSet<Node>> = HashMap()
    ) {
        override fun equals(other: Any?): Boolean {
            val o = other as? Node ?: return false
            return o.id == id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun toString(): String {
            return "$char ($id)"
        }
    }

    private val idCounter = AtomicInteger(0)
    private val rootNode: Node = Node(idCounter.getAndIncrement(), '@', false, null)

    fun add(vararg words: String) {
        words.forEach(::add)
    }

    fun add(word: String) {
        var currentNode = rootNode
        val capitalNodes = mutableSetOf<Node>()
        for (c in word) {
            val child = currentNode.next[c]?.find { it.parent == currentNode } // TODO store link to a direct child
            currentNode = child ?: let {
                val newNode = Node(idCounter.getAndIncrement(), c, false, currentNode)
                currentNode.next.compute(c) { _, l -> (l ?: mutableSetOf()).also { it.add(newNode) } }
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
                curNode.next.compute(capNode.char) { _, l -> (l ?: mutableSetOf()).also { it.add(capNode) } }
                curNode = curNode.parent
            }
        }
    }

    fun search(term: String): List<String> {
        val result = mutableListOf<String>()
        val nodesToSearch = LinkedList<Pair<Int, Node>>()
        nodesToSearch.push(0 to rootNode)

        while (nodesToSearch.isNotEmpty()) {
            val (curIndex, node) = nodesToSearch.pop()

            val nextNodes = if (curIndex < term.length) {
                // Allow jump to capital letters.
                node.next[term[curIndex]] ?: emptyList()
            } else {
                if (node.isTerminal) {
                    result.add(buildWord(node))
                }

                // Go only to direct descendants.
                node.next.values.mapNotNull { n -> n.find { it.parent == node } } // TODO store link to a direct child
            }
            for (child in nextNodes) {
                nodesToSearch.push(curIndex + 1 to child)
            }
        }

        return result
    }

    private fun buildWord(node: Node): String {
        val result = StringBuilder()
        result.append(node.char)
        var curNode = node.parent
        while (curNode != rootNode && curNode != null) {
            result.append(curNode.char)
            curNode = curNode.parent
        }
        return result.reverse().toString()
    }
}
