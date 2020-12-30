package suive.kotlinls.method

import suive.kotlinls.model.NoParams
import suive.kotlinls.model.NullResult

class NoOpMethod : Method<NoParams, NullResult>() {
    override fun doProcess(request: Request, params: NoParams) = NullResult(request)
}
