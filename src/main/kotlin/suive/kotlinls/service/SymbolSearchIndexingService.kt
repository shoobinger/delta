package suive.kotlinls.service

import suive.kotlinls.data.SymbolSearchGraph

/**
 * Naive implementation of the indexing service using a SymbolSearchGraph data structure.
 */
class SymbolSearchIndexingService : IndexingService {

    private val index = SymbolSearchGraph()

    init {
        index.add("class", "interface", "val")
        index.add("String")
    }
}
