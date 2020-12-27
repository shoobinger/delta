package suive.kotlinls.service

import suive.kotlinls.model.Position
import java.nio.file.Path

class CompletionService(
    private val compilerService: CompilerService
) {

    // TODO This method should not read from disk, but from an in-memory workspace file tree.
    fun getCompletions(file: Path, position: Position): List<String> {
        val parsed = compilerService.parseFile(file.toFile().readText())
        val zero = parsed.findElementAt(0)
        println(zero)
        return emptyList()
    }
}
