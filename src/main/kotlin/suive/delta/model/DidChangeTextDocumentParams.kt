package suive.delta.model

data class DidChangeTextDocumentParams(
    val textDocument: TextDocumentIdentifier,
    val contentChanges: List<TextDocumentContentChangeEvent>
): Params
