package suive.kotlinls.model

import suive.kotlinls.method.Request

data class InitializeResult(
    override val request: Request,
    val capabilities: ServerCapabilities
) : Output.Result(request)
