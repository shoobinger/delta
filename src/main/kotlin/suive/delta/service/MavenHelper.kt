package suive.delta.service

import suive.delta.getTempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class MavenHelper {

    fun collectDependencies(pom: Path): List<Path> {
        val outputFile = Files.createTempFile(Paths.get(getTempDir()), "delta-maven-dependency-list", null)
        ProcessBuilder()
            .directory(pom.parent.toFile())
            .command(
                "mvn", "dependency:list", "-DoutputAbsoluteArtifactFilename=true",
                "-DoutputFile=${outputFile.toAbsolutePath()}"
            )
            .inheritIO()
            .start()
            .waitFor(1, TimeUnit.MINUTES)

        return Files.readAllLines(outputFile)
            .mapNotNull { line ->
                // Line format:
                // groupId:artifactId:classifier:version:scope:absoluteJarPath
                line
                    .split(":")
                    .takeIf { it.size == 6 } // Skip non-relevant lines.
                    ?.get(5) // Get the last part.
                    ?.let { Paths.get(it) }
            }
    }
}
