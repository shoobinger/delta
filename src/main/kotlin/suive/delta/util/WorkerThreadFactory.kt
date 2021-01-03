package suive.delta.util

import org.tinylog.kotlin.Logger
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(private val prefix: String) : ThreadFactory {
    private val threadNumber = AtomicInteger(0)

    private val loggingExceptionHandler = Thread.UncaughtExceptionHandler { t, e ->
        Logger.error(e, "Uncaught exception")
    }

    override fun newThread(r: Runnable) =
        Thread(null, r, "$prefix${threadNumber.getAndIncrement()}").apply {
            uncaughtExceptionHandler = loggingExceptionHandler
        }
}
