package suive.delta.model

enum class DiagnosticSeverity(val num: Int) {
    Error(1),
    Warning(2),
    Information(3),
    Hint(4)
}

data class Diagnostic(
    val range: Range,
    val severity: Int,
    val message: String
)
