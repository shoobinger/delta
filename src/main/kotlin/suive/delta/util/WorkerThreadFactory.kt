package suive.delta.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(private val prefix: String) : ThreadFactory {
    private val threadNumber = AtomicInteger(0)

    override fun newThread(r: Runnable) =
        Thread(null, r, "$prefix${threadNumber.getAndIncrement()}")
}
