package suive.kotlinls.task

interface Task<R> {
    fun execute(): R
}
