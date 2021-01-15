package suive.delta

import org.tinylog.kotlin.Logger
import kotlin.time.measureTimedValue

fun getTempDir(): String = System.getenv("TEMP_DIR") ?: System.getProperty("java.io.tmpdir")

inline fun <T> executeTimed(operation: String, block: () -> T): T {
    val (result, time) = measureTimedValue(block)
    Logger.debug { "Executed [$operation] for in $time" }
    return result
}
