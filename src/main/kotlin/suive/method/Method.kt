package suive.method

import suive.model.Output
import suive.model.Params

abstract class Method<P : Params, out R : Output.Result> {
    abstract fun doProcess(request: Request, params: P): R
}
