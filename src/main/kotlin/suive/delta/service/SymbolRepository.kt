package suive.delta.service

import io.github.classgraph.ClassGraph
import suive.delta.Workspace
import suive.delta.data.SymbolSearchGraph
import java.nio.file.Path
import java.nio.file.Paths

class SymbolRepository() {

    private val globalIndex = SymbolSearchGraph()

    fun indexClasses(classpath: List<Path>) {
        ClassGraph()
            .overrideClasspath(
                listOf(Paths.get("/home/ivan/Desktop/javabase").toFile())
                    + classpath
            )
            .enableClassInfo()
//            .enableFieldInfo()
//            .enableMethodInfo()
            .scan().use { scanResult ->
                scanResult.allClasses.forEach {
                    globalIndex.add(it.simpleName)
                }
            }
    }

    fun search(term: String): List<String> {
        return globalIndex.search(term)
    }
}
