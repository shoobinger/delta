package suive.delta

fun getTempDir(): String = System.getenv("TEMP_DIR") ?: System.getProperty("java.io.tmpdir")
