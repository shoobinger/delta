package suive.method

import suive.model.Params
import suive.model.Result

abstract class Method<P : Params, out R : Result> {
    abstract fun doProcess(params: P): R
}
