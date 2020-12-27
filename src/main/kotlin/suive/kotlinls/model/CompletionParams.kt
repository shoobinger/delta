package suive.kotlinls.model

data class CompletionParams(
    val textDocument: TextDocumentIdentifier,
    val position: Position
) : Params
