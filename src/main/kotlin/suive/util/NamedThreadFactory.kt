package suive.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(private val namePrefix: String) : ThreadFactory {
    private val threadNumber = AtomicInteger(0)

    override fun newThread(r: Runnable) = Thread(null, r, "$namePrefix-${threadNumber.getAndIncrement()}")
}
