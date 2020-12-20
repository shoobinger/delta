package suive.method

import suive.ClientHandle
import suive.JSON_MAPPER
import suive.model.InitializeParams
import suive.model.InitializeResult
import suive.model.ServerCapabilities
import suive.service.DiagnosticService

class InitializeMethod(
    private val clientHandle: ClientHandle
) : Method<InitializeParams, InitializeResult>(
    InitializeParams::class, JSON_MAPPER
) {
    private val diagnosticService = DiagnosticService()

    override fun doProcess(params: InitializeParams): InitializeResult {
        val initializeResult = InitializeResult(ServerCapabilities)
        // TODO Build in-memory tree of files.
        // TODO Perform diagnostics and send notification to the client.
        return initializeResult
    }
}
