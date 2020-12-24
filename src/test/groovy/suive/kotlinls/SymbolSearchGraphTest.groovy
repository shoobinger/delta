package suive.kotlinls

import org.junit.jupiter.api.Test
import suive.kotlinls.data.SymbolSearchGraph

class SymbolSearchGraphTest {

    @Test
    void "should query by prefix"() {
        def graph = new SymbolSearchGraph()
        graph.add("String", "Strange", "List", "ArrayList", "LinkedList")

        assert graph.search("").sort() == ["ArrayList", "LinkedList", "List", "Strange", "String"].sort()
        assert graph.search("S").sort() == ["Strange", "String"].sort()
        assert graph.search("St").sort() == ["Strange", "String"].sort()
        assert graph.search("Str").sort() == ["Strange", "String"].sort()
        assert graph.search("Stri") == ["String"]
        assert graph.search("Stra") == ["Strange"]
        assert graph.search("String") == ["String"]
        assert graph.search("X") == []
        assert graph.search("Array").sort() == ["ArrayList"].sort()
    }

    @Test
    void "should query using abbreviations"() {
        def graph = new SymbolSearchGraph()
        graph.add("LogStream", "LongStream", "LongStreamTest", "LongStreamFactory", "List")

        assert graph.search("LSt").sort() == ["LogStream", "LongStream", "LongStreamTest", "LongStreamFactory"].sort()
        assert graph.search("LSF").sort() == ["LongStreamFactory"]
        assert graph.search("LT").sort() == ["LongStreamTest"]
        assert graph.search("LTest").sort() == ["LongStreamTest"]
        assert graph.search("LTet").sort() == []
        assert graph.search("LonT").sort() == ["LongStreamTest"]
        assert graph.search("LonTes").sort() == ["LongStreamTest"]
        assert graph.search("LonStrTe").sort() == ["LongStreamTest"]
        assert graph.search("LonTest").sort() == ["LongStreamTest"]
        assert graph.search("LongStreamTest").sort() == ["LongStreamTest"]
        assert graph.search("LongStream").sort() == ["LongStream", "LongStreamTest", "LongStreamFactory"].sort()
    }

    @Test
    void "should query by substring"() {
        def tree = new SymbolSearchGraph()
        tree.add("LogStream", "LongStream", "LongStreamTest")

        assert tree.search("tream").sort() == ["LogStream", "LongStream", "LongStreamTest"]
    }

    @Test
    void "should be able to delete entries"() {
    }
}
