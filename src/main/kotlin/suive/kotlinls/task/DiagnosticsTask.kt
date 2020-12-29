package suive.kotlinls.task

import suive.kotlinls.model.PublishDiagnosticsParams
import suive.kotlinls.service.CompilerService
import suive.kotlinls.util.DiagnosticMessageCollector
import java.nio.file.Paths

class DiagnosticsTask(
    private val compilerService: CompilerService,
    private val rootUri: String
) : NotificationTask<PublishDiagnosticsParams> {
    override fun method() = "textDocument/publishDiagnostics"
    override fun execute(): List<PublishDiagnosticsParams> {
        // TODO Build in-memory tree of files.
        val rootPath = Paths.get(rootUri)

        val messageCollector = DiagnosticMessageCollector()
        compilerService.compile(rootPath, rootPath.resolve("src"), messageCollector)

        return if (messageCollector.diagnostics.isEmpty()) emptyList() else
            messageCollector.diagnostics.groupBy({ it.first }, { it.second }).map { (t, u) ->
                PublishDiagnosticsParams(t, u)
            }
    }
}
