package suive.delta.model

data class DidChangeWatchedFilesRegistrationOptions(
    val watchers: List<FileSystemWatcher>
)

data class FileSystemWatcher(
    val globPattern: String
)

data class DidChangeWatchedFilesParams(
    val changes: List<FileEvent>
)

data class FileEvent(
    val uri: String,
    val type: Int
)
