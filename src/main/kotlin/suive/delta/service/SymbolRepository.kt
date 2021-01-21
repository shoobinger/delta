package suive.delta.service

import io.github.classgraph.ClassGraph
import suive.delta.data.SymbolSearchGraph
import java.nio.file.Path
import java.nio.file.Paths

class SymbolRepository {

    private val globalIndex = SymbolSearchGraph()

    fun indexClasses(classpath: List<Path>) {
        ClassGraph()
//                .acceptModules("java.base")
//                .disableDirScanning()
//                .j
//                .enableSystemJarsAndModules()
            .overrideClasspath(
                listOf(Paths.get("/home/ivan/Desktop/javabase").toFile())
                    + classpath
            )
//            .verbose()
//            .enableSystemJarsAndModules()
//                .overrideClasspath(workspace.classpath)
//                .jars("/usr/lib/jvm/java-15-openjdk/jmods/java.base.jmod")
//            .acceptPaths("/usr/lib/jvm/java-15-openjdk")
//            .overrideClassLoaders(ClassLoader().)
//            .overrideClassLoaders(object : ClassLoader() {
//
//            })
//            .addModuleLayer(ModuleLayer())
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
        return globalIndex.search(term)
    }
}
