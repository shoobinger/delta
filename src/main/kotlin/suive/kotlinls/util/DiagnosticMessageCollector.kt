package suive.kotlinls.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.tinylog.kotlin.Logger
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
        Logger.debug(message)
        val diagnosticSeverity = toDiagnosticSeverity(severity)
        if (diagnosticSeverity != null && location != null) {
            diagnostics += location.path to
                Diagnostic(
                    range = Range(
                        start = Position(location.line - 1, location.column - 1),
                        end = Position(location.lineEnd - 1, location.columnEnd - 1)
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
