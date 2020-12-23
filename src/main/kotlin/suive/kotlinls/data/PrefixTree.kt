package suive.kotlinls.data

import java.util.LinkedList

class PrefixTree<V> {

    private data class Node<V>(
        val char: Char,
        var values: List<V>,
        val children: Array<Node<V>?>
    )

    private val rootNode: Node<V> = Node('@', emptyList(), arrayOfNulls(255))

    fun add(input: String, values: List<V>) {
        var currentNode = rootNode
        for (c in input.toLowerCase()) {
            val child = currentNode.children[c.toInt()]
            currentNode = child ?: let {
                val newNode = Node<V>(c, emptyList(), arrayOfNulls(255))
                currentNode.children[c.toInt()] = newNode
                newNode
            }
        }
        currentNode.values = values
    }

    fun query(prefix: String): List<V> {
        val lowerPrefix = prefix.toLowerCase()
        var currentNode = rootNode
        for (c in lowerPrefix) {
            currentNode = currentNode.children[c.toInt()] ?: return emptyList()
        }

        val result = mutableSetOf<V>()
        val nodesToSearch = LinkedList<Node<V>>()
        nodesToSearch.push(currentNode)

        while (nodesToSearch.isNotEmpty()) {
            val (_, values, children) = nodesToSearch.pop()
            result.addAll(values)

            for (i in (children.size - 1) downTo 0) {
                val child = children[i]
                if (child != null) {
                    nodesToSearch.push(child)
                }
            }
        }

        return result.toList()
    }

    fun delete(w: String) {

    }
}
