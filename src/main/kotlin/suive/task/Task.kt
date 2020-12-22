package suive.task

import suive.model.Params

interface Task<P : Params> {
    fun execute(): List<P>
    fun method(): String
}
