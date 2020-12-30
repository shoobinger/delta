package suive.kotlinls.model

data class DidChangeTextDocumentParams(
    val textDocument: TextDocumentIdentifier,
    val contentChanges: List<TextDocumentContentChangeEvent>
): Params
