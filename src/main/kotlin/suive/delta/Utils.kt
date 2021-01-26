package suive.delta

import org.tinylog.kotlin.Logger
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.time.measureTimedValue

fun getTempDir(): String = System.getenv("TEMP_DIR") ?: System.getProperty("java.io.tmpdir")

inline fun <T> executeTimed(operation: String, block: () -> T): T {
    val (result, time) = measureTimedValue(block)
    Logger.debug { "Executed [$operation] for in $time" }
    return result
}

fun unzip(source: Path, target: Path, startFrom: String) {
    Files.createDirectories(target)
    FileSystems.newFileSystem(source)
        .use { fs ->
            val from = fs.getPath(startFrom)
            val root = fs.rootDirectories.first().resolve(from)
            Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    val dirToCreate = Paths.get(target.toString(), from.relativize(dir).toString())
                    if (Files.notExists(dirToCreate)) {
                        Files.createDirectory(dirToCreate)
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    val fileToCreate = Paths.get(target.toString(), from.relativize(file).toString())
                    Files.copy(file, fileToCreate, StandardCopyOption.REPLACE_EXISTING)
                    return FileVisitResult.CONTINUE
                }
            })
        }
}
