package suive.delta.service

import suive.delta.util.NamedThreadFactory
import java.util.concurrent.Executors

class TaskService {
    private val executor = Executors.newCachedThreadPool(NamedThreadFactory("Task-"))

    fun execute(runnable: Runnable) {
        executor.submit(runnable)
    }
}
