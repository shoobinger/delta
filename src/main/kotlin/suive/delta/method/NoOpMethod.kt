package suive.delta.method

import suive.delta.model.NoParams
import suive.delta.model.NullResult

class NoOpMethod : Method<NoParams, NullResult>() {
    override fun doProcess(request: Request, params: NoParams) = NullResult(request)
}
