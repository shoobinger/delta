package suive.delta.task

import org.tinylog.kotlin.Logger
import suive.delta.Workspace
import suive.delta.service.MavenClasspathCollector
import java.nio.file.Files

class UpdateClasspathTask(
    private val mavenClasspathCollector: MavenClasspathCollector,
    private val workspace: Workspace
) : Task<Unit> {
    override fun execute() {
        val pom = workspace.internalRoot.resolve("pom.xml")
        val classpath = if (Files.notExists(pom)) {
            emptyList()
        } else {
            Logger.info { "Found pom.xml, resolving classpath" }
            mavenClasspathCollector.collect(pom)
        }
        workspace.updateClasspath(classpath)
    }
}
