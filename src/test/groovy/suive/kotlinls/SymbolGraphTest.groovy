package suive.kotlinls

import org.junit.jupiter.api.Test
import suive.kotlinls.data.SymbolGraph

class SymbolGraphTest {

    @Test
    void "should query by prefix"() {
        def tree = new SymbolGraph()
        tree.add("String", "Strange", "List", "ArrayList", "LinkedList")

        assert tree.query("").sort() == ["ArrayList", "LinkedList", "List", "Strange", "String"].sort()
        assert tree.query("S").sort() == ["Strange", "String"].sort()
        assert tree.query("St").sort() == ["Strange", "String"].sort()
        assert tree.query("Str").sort() == ["Strange", "String"].sort()
        assert tree.query("Stri") == ["String"]
        assert tree.query("Stra") == ["Strange"]
        assert tree.query("String") == ["String"]
        assert tree.query("X") == []
        assert tree.query("Array").sort() == ["ArrayList"].sort()
    }

    @Test
    void "should query using abbreviations"() {
        def tree = new SymbolGraph()
        tree.add("LogStream", "LongStream", "LongStreamTest", "LongStreamFactory", "List")

        assert tree.query("LSt").sort() == ["LogStream", "LongStream", "LongStreamTest", "LongStreamFactory"].sort()
        assert tree.query("LSF").sort() == ["LongStreamFactory"]
        assert tree.query("LT").sort() == ["LongStreamTest"]
        assert tree.query("LTest").sort() == ["LongStreamTest"]
        assert tree.query("LTet").sort() == []
        assert tree.query("LonT").sort() == ["LongStreamTest"]
        assert tree.query("LonTes").sort() == ["LongStreamTest"]
        assert tree.query("LonStrTe").sort() == ["LongStreamTest"]
    }

    @Test
    void "should query by substring"() {
        def tree = new SymbolGraph()
        tree.add("LogStream", "LongStream", "LongStreamTest")

        assert tree.query("tream").sort() == ["LogStream", "LongStream", "LongStreamTest"]
    }

    @Test
    void "should be able to delete entries"() {
    }
}
