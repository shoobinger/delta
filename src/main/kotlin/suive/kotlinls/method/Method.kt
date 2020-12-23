package suive.kotlinls.method

import suive.kotlinls.model.Output
import suive.kotlinls.model.Params

abstract class Method<P : Params, out R : Output.Result> {
    abstract fun doProcess(request: Request, params: P): R
}
