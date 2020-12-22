package suive.task

import suive.model.PublishDiagnosticsParams
import suive.service.DiagnosticService

class DiagnosticsTask(
    private val diagnosticService: DiagnosticService
) : Task<PublishDiagnosticsParams> {
    override fun method() = "textDocument/publishDiagnostics"
    override fun execute(): List<PublishDiagnosticsParams> {
        // TODO Build in-memory tree of files.
        return listOf(
            PublishDiagnosticsParams(
                "",
                diagnosticService.perform("")
            )
        )
    }
}
