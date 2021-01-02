package suive.delta.model

data class TextDocumentContentChangeEvent(
    val range: Range,
    val text: String
)
