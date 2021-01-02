package suive.delta.model

import suive.delta.method.Request

data class NullResult(
    override val request: Request
) : Output.Result(request)
