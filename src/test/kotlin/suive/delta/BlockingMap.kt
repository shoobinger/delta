package suive.delta

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

class BlockingMap<K, V> {
    private val map: ConcurrentMap<K, BlockingQueue<V>> = ConcurrentHashMap()

    private fun getQueue(key: K, replace: Boolean): BlockingQueue<V> {
        return map.compute(key) { _, queue ->
            if (queue == null) ArrayBlockingQueue(1) else {
                if (replace)
                    queue.clear()
                queue
            }
        } as BlockingQueue<V>
    }

    operator fun set(key: K, value: V) {
        getQueue(key, true).add(value)
    }

    operator fun get(key: K): V {
        return getQueue(key, false).take()
    }

    fun get(key: K, timeout: Long, unit: TimeUnit): V? {
        return getQueue(key, false).poll(timeout, unit)
    }
}
