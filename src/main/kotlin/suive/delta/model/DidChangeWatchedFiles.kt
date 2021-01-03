package suive.delta.model

data class DidChangeWatchedFilesRegistrationOptions(
    val watchers: List<FileSystemWatcher>
)

data class FileSystemWatcher(
    val globPattern: String
)
