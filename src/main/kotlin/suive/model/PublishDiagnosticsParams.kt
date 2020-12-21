package suive.model

data class PublishDiagnosticsParams(
    val uri: String,
    val diagnostics: List<Diagnostic>
)
