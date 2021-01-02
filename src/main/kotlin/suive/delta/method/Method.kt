package suive.delta.method

import suive.delta.model.Output
import suive.delta.model.Params

abstract class Method<P : Params, out R : Output.Result> {
    abstract fun doProcess(request: Request, params: P): R
}
