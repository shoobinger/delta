package suive.delta

import org.junit.jupiter.api.Test
import suive.delta.data.SymbolSearchGraph

class SymbolSearchGraphTest {

    @Test
    fun `should query by prefix`() {
        val graph = SymbolSearchGraph()
        graph.add("String", "Strange", "List", "ArrayList", "LinkedList")

        assert(graph.search("").sorted() == listOf("ArrayList", "LinkedList", "List", "Strange", "String").sorted())
        assert(graph.search("S").sorted() == listOf("Strange", "String").sorted())
        assert(graph.search("St").sorted() == listOf("Strange", "String").sorted())
        assert(graph.search("Str").sorted() == listOf("Strange", "String").sorted())
        assert(graph.search("Stri") == listOf("String"))
        assert(graph.search("Stra") == listOf("Strange"))
        assert(graph.search("String") == listOf("String"))
        assert(graph.search("X") == emptyList<String>())
        assert(graph.search("Array").sorted() == listOf("ArrayList").sorted())
    }

    @Test
    fun `should query using abbreviations`() {
        val graph = SymbolSearchGraph()
        graph.add("LogStream", "LongStream", "LongStreamTest", "LongStreamFactory", "List")

        assert(
            graph.search("LSt").sorted() == listOf(
                "LogStream",
                "LongStream",
                "LongStreamTest",
                "LongStreamFactory"
            ).sorted()
        )
        assert(graph.search("LSF").sorted() == listOf("LongStreamFactory"))
        assert(graph.search("LT").sorted() == listOf("LongStreamTest"))
        assert(graph.search("LTest").sorted() == listOf("LongStreamTest"))
        assert(graph.search("LTet").sorted() == emptyList<String>())
        assert(graph.search("LonT").sorted() == listOf("LongStreamTest"))
        assert(graph.search("LonTes").sorted() == listOf("LongStreamTest"))
        assert(graph.search("LonStrTe").sorted() == listOf("LongStreamTest"))
        assert(graph.search("LonTest").sorted() == listOf("LongStreamTest"))
        assert(graph.search("LongStreamTest").sorted() == listOf("LongStreamTest"))
        assert(
            graph.search("LongStream").sorted() == listOf(
                "LongStream",
                "LongStreamTest",
                "LongStreamFactory"
            ).sorted()
        )
    }

    @Test
    fun `should query by substring`() {
        val tree = SymbolSearchGraph()
        tree.add("LogStream", "LongStream", "LongStreamTest")

        assert(tree.search("tream").sorted() == listOf("LogStream", "LongStream", "LongStreamTest"))
    }

    @Test
    fun `should be able to delete entries`() {
    }
}
