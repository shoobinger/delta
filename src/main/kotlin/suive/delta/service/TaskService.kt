package suive.delta.service

import org.tinylog.kotlin.Logger
import suive.delta.util.NamedThreadFactory
import java.util.concurrent.Executors

class TaskService {
    private val executor = Executors.newCachedThreadPool(NamedThreadFactory("Task-"))

    fun execute(runnable: Runnable) {
        executor.submit {
            try {
                runnable.run()
            } catch (e: Exception) {
                Logger.error(e)
            }
        }
    }
}
