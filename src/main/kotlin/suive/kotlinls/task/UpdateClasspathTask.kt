package suive.kotlinls.task

import suive.kotlinls.service.CompilerService
import suive.kotlinls.service.MavenClasspathCollector
import java.nio.file.Paths

class UpdateClasspathTask(
    private val mavenClasspathCollector: MavenClasspathCollector,
    private val compilerService: CompilerService
) : Task<Unit> {
    override fun execute() {
        val classpath =
            mavenClasspathCollector.collect(Paths.get("/home/ivan/projects/kotlin-ls/src/test/resources/test-projects/maven/pom.xml"))
        compilerService.updateClasspath(classpath)
    }
}
