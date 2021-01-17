package suive.delta.service

import io.github.classgraph.ClassGraph
import org.tinylog.kotlin.Logger
import suive.delta.Workspace
import suive.delta.data.SymbolSearchGraph

class GlobalSearchService(
    private val workspace: Workspace
) {
    private val globalIndex = SymbolSearchGraph()

    fun indexClasses() {
        ClassGraph()
//            .verbose()
//            .enableSystemJarsAndModules()
            .overrideClasspath(workspace.classpath)
//            .also { classGraph ->
//                Logger.info{workspace.classpath.toString()}
//                workspace.classpath.forEach {
//                    classGraph.jar(it.absolutePath)
//                }
//            }
            .enableClassInfo()
            .enableFieldInfo()
            .enableMethodInfo()
            .scan().use { scanResult ->
                scanResult.allClasses.forEach {
                    globalIndex.add(it.simpleName)
                }
            }
    }

    fun search(term: String): List<String> {
        indexClasses()
        return globalIndex.search(term)
    }
}
