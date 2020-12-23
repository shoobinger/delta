package suive.kotlinls

import org.junit.jupiter.api.Test
import suive.kotlinls.data.PrefixTree

class PrefixTreeTest {

    @Test
    void "should query by prefix"() {
        def tree = new PrefixTree()
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
        def tree = new PrefixTree()
        tree.add("LogStream", "LongStream", "LongStreamTest")

        assert tree.query("Lst").sort() == ["LogStream", "LongStream", "LongStreamTest"]
        assert tree.query("LT").sort() == ["LongStreamTest"]
        assert tree.query("LTest").sort() == ["LongStreamTest"]
        assert tree.query("LTet").sort() == []
        assert tree.query("Lst").sort() == ["LogStream", "LongStream", "LongStreamTest"]
        assert tree.query("Lont").sort() == ["LongStreamTest"]
        assert tree.query("Lonts").sort() == ["LongStreamTest"]
        assert tree.query("LonStrTe").sort() == ["LongStreamTest"]
    }

    @Test
    void "should query by substring"() {
        def tree = new PrefixTree()
        tree.add("LogStream", "LongStream", "LongStreamTest")

        assert tree.query("tream").sort() == ["LogStream", "LongStream", "LongStreamTest"]
    }

    @Test
    void "should be able to delete entries"() {
    }
}
