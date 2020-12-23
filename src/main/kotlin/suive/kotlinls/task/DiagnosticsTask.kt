package suive.kotlinls.task

import suive.kotlinls.model.PublishDiagnosticsParams
import suive.kotlinls.service.DiagnosticService

class DiagnosticsTask(
    private val diagnosticService: DiagnosticService
) : NotificationTask<PublishDiagnosticsParams> {
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
