package suive.kotlinls

import org.junit.jupiter.api.Test
import suive.kotlinls.data.PrefixTree

class PrefixTreeTest {

    @Test
    void "should query by prefix"() {
        def tree = new PrefixTree<String>()
        tree.add("String", ["String"])
        tree.add("Strange", ["Strange"])
        tree.add("List", ["ArrayList<E>", "LinkedList<E>", "List<E>"])
        tree.add("ArrayList", ["ArrayList<E>"])

        assert tree.query("").sort() == ["ArrayList<E>", "LinkedList<E>", "List<E>", "Strange", "String"].sort()
        assert tree.query("S").sort() == ["Strange", "String"].sort()
        assert tree.query("St").sort() == ["Strange", "String"].sort()
        assert tree.query("Str").sort() == ["Strange", "String"].sort()
        assert tree.query("Stri") == ["String"]
        assert tree.query("Stra") == ["Strange"]
        assert tree.query("String") == ["String"]
        assert tree.query("X") == []
        assert tree.query("List").sort() == ["ArrayList<E>", "LinkedList<E>", "List<E>"].sort()
    }

    @Test
    void "should query case-insensitively"() {
        def tree = new PrefixTree<String>()
        tree.add("String", ["String"])
        tree.add("Strange", ["Strange"])

        assert tree.query("st").sort() == tree.query("St").sort()
    }

    @Test
    void "should be able to delete entries"() {

    }
}
