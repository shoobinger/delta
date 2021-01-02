package suive.delta.task

interface Task<R> {
    fun execute(): R
}
