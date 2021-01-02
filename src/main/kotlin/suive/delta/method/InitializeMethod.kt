package suive.delta.method

import org.tinylog.kotlin.Logger
import suive.delta.Workspace
import suive.delta.model.InitializeParams
import suive.delta.model.InitializeResult
import suive.delta.model.ServerCapabilities
import java.net.URI
import java.nio.file.Paths

class InitializeMethod(private val workspace: Workspace) : Method<InitializeParams, InitializeResult>() {
    override fun doProcess(request: Request, params: InitializeParams): InitializeResult {
        if (params.rootUri != null) {
            Logger.info { "Initializing workspace ${params.rootUri}" }
            workspace.initialize(Paths.get(URI(params.rootUri)))
//            workspace.startDiagnostics()
        }

        return InitializeResult(request, ServerCapabilities())
    }
}
