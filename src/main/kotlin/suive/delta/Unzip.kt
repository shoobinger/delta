package suive.delta

import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes


fun main() {
    unzip(Paths.get("/usr/lib/jvm/java-12-j9/jmods/java.base.jmod"), Paths.get("result"), "classes")
}
