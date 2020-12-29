package suive.kotlinls.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import suive.kotlinls.model.Diagnostic
import suive.kotlinls.model.DiagnosticSeverity
import suive.kotlinls.model.Position
import suive.kotlinls.model.Range

class DiagnosticMessageCollector : MessageCollector {
    val diagnostics = mutableListOf<Pair<String, Diagnostic>>()

    override fun clear() {
        diagnostics.clear()
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        val diagnosticSeverity = toDiagnosticSeverity(severity)
        if (diagnosticSeverity != null && location != null) {
            diagnostics += location.path to
                Diagnostic(
                    range = Range(
                        start = Position(location.line, location.column),
                        end = Position(location.lineEnd, location.columnEnd)
                    ),
                    severity = diagnosticSeverity.num,
                    message = message
                )
        }
    }

    override fun hasErrors(): Boolean =
        diagnostics.any { (_, diag) -> diag.severity == DiagnosticSeverity.Error.num }

    private fun toDiagnosticSeverity(compilerMessageSeverity: CompilerMessageSeverity): DiagnosticSeverity? {
        return when (compilerMessageSeverity) {
            CompilerMessageSeverity.EXCEPTION, CompilerMessageSeverity.ERROR, CompilerMessageSeverity.STRONG_WARNING -> DiagnosticSeverity.Error
            CompilerMessageSeverity.WARNING -> DiagnosticSeverity.Warning
            else -> null
        }
    }
}
