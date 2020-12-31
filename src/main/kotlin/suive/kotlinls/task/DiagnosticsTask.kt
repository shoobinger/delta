package suive.kotlinls.task

import org.tinylog.kotlin.Logger
import suive.kotlinls.Workspace
import suive.kotlinls.model.PublishDiagnosticsParams
import suive.kotlinls.service.CompilerService
import suive.kotlinls.util.DiagnosticMessageCollector
import java.io.File
import java.net.URI
import java.nio.file.Paths

class DiagnosticsTask(
    private val compilerService: CompilerService,
    private val workspace: Workspace,
    private val rootUri: String
) : NotificationTask<PublishDiagnosticsParams> {
    override fun method() = "textDocument/publishDiagnostics"
    override fun execute(): List<PublishDiagnosticsParams> {
        val sourceUri = Paths.get(URI(rootUri)).resolve("src").toUri().toString() // TODO simplify
        val messageCollector = DiagnosticMessageCollector(workspace)
        compilerService.compile(rootUri, sourceUri, messageCollector)

        return if (messageCollector.diagnostics.isEmpty()) emptyList() else
            messageCollector.diagnostics.groupBy({ it.first }, { it.second }).map { (t, u) ->
                PublishDiagnosticsParams(t, u)
            }
    }
}
