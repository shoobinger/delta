package suive.delta.task

import suive.delta.service.MavenClasspathCollector

class UpdateClasspathTask(
    private val mavenClasspathCollector: MavenClasspathCollector
) : Task<Unit> {
    override fun execute() {
//        val classpath =
//            mavenClasspathCollector.collect(Paths.get("/home/ivan/projects/kotlin-ls/src/test/resources/test-projects/maven/pom.xml"))
//        workspace.updateClasspath(classpath)
    }
}
