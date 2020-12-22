package suive.model

import suive.method.Request

data class InitializeResult(
    override val request: Request,
    val capabilities: ServerCapabilities
) : Output.Result(request)
