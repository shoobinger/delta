package suive.kotlinls.model

import suive.kotlinls.method.Request

data class NullResult(
    override val request: Request
) : Output.Result(request)
