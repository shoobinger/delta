package suive.kotlinls.model

import suive.kotlinls.model.Range

data class TextDocumentContentChangeEvent(
    val range: Range,
    val text: String
)
