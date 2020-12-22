package suive.method

import suive.model.InitializeParams
import suive.model.InitializeResult
import suive.model.ServerCapabilities

class InitializeMethod : Method<InitializeParams, InitializeResult>() {
    override fun doProcess(params: InitializeParams) = InitializeResult(ServerCapabilities)
}
