package suive.delta.model

import suive.delta.Request

data class InitializeResult(
    override val request: Request,
    val capabilities: ServerCapabilities
) : Output.Result(request)
