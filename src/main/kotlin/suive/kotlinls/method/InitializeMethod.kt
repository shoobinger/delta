package suive.kotlinls.method

import suive.kotlinls.model.InitializeParams
import suive.kotlinls.model.InitializeResult
import suive.kotlinls.model.ServerCapabilities

class InitializeMethod : Method<InitializeParams, InitializeResult>() {
    override fun doProcess(request: Request, params: InitializeParams) =
        InitializeResult(request, ServerCapabilities)
}
