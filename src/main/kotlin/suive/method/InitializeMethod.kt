package suive.method

import suive.JSON_MAPPER
import suive.model.InitializeParams
import suive.model.InitializeResult
import suive.model.ServerCapabilities

class InitializeMethod : Method<InitializeParams, InitializeResult>(
    InitializeParams::class, JSON_MAPPER
) {
    override fun doProcess(params: InitializeParams): InitializeResult {
        return InitializeResult(ServerCapabilities)
    }
}
