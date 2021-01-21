package suive.delta.service

data class BuildRequest(
    val cleanBuild: Boolean = false,
    val buildDelay: Long = 0L
)
