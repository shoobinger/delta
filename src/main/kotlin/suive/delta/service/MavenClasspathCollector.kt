package suive.delta.service

import com.jcabi.aether.Aether
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
import org.apache.maven.artifact.repository.MavenArtifactRepository
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.ModelProblemCollector
import org.apache.maven.model.interpolation.StringSearchModelInterpolator
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.project.MavenProject
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.sonatype.aether.util.artifact.JavaScopes
import java.io.FileReader
import java.nio.file.Path
import java.nio.file.Paths

class MavenClasspathCollector {

    fun collect(pom: Path): List<Path> {
        val mavenReader = MavenXpp3Reader()
        val reader = FileReader(pom.toFile())

        val modelBuildingRequest = DefaultModelBuildingRequest()
        val problems = ModelProblemCollector { severity, message, location, cause -> }
        val model = StringSearchModelInterpolator().interpolateModel(
            mavenReader.read(reader), pom.parent.toFile(),
            modelBuildingRequest, problems
        )

        val project = MavenProject(model)
        project.remoteArtifactRepositories = mutableListOf(
            MavenArtifactRepository(
                "central", "https://repo1.maven.org/maven2/", DefaultRepositoryLayout(),
                ArtifactRepositoryPolicy(), ArtifactRepositoryPolicy()
            ) as ArtifactRepository
        )
        val aether = Aether(project, Paths.get(System.getProperty("user.home"), ".m2/repository").toFile())

        return project.dependencies.flatMap { dep ->
            aether.resolve(
                DefaultArtifact(dep.groupId, dep.artifactId, dep.classifier, dep.type, dep.version),
                JavaScopes.COMPILE
            ).map { it.file.toPath() }
        }
    }
}
