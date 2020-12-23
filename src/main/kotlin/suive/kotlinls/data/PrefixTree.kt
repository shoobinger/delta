package suive.kotlinls.data

import java.util.LinkedList

class PrefixTree {

    private data class Node(
        val char: Char,
        var isTerminal: Boolean,
        val children: Array<Node?>,
        val parent: Node? = null
    )

    private val rootNode: Node = Node('@', false, arrayOfNulls(255))

    fun add(vararg words: String) {
        words.forEach(::add)
    }

    fun add(word: String) {
        var currentNode = rootNode
        val capitalNodes = mutableSetOf<Node>()
        for (c in word) {
            val child = currentNode.children[c.toInt()]
            currentNode = child ?: let {
                val newNode = Node(c, false, arrayOfNulls(255), currentNode)
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
            var curNode = capNode
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

            for (i in (node.children.size - 1) downTo 0) {
                val child = node.children[i]
                if (child != null) {
                    nodesToSearch.push(nextPrefix to child)
                }
            }
        }

        return result
    }

    fun delete(w: String) {

    }
}
