package suive.kotlinls

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

class BlockingMap<K, V> {
    private ConcurrentMap<K, BlockingQueue<V>> map = new ConcurrentHashMap()

    private BlockingQueue<V> getQueue(K key, boolean replace) {
        return map.compute(key) { _, v -> if (v == null) new ArrayBlockingQueue(1) else v } as BlockingQueue<V>
    }

    def set(K key, V value) {
        getQueue(key, true).add(value)
    }

    V get(K key) {
        return getQueue(key, false).take()
    }

    V get(K key, long timeout, TimeUnit unit) {
        return getQueue(key, false).poll(timeout, unit)
    }
}
