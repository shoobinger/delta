package suive.kotlinls.service

import suive.kotlinls.model.Position
import java.nio.file.Path

class CompletionService(
    private val compilerService: CompilerService
) {

    // TODO This method should not read from disk, but from an in-memory workspace file tree.
    fun getCompletions(file: Path, position: Position): List<String> {
        val text = file.toFile().readText()
        val parsed = compilerService.parseFile(text)
        val zero = parsed.findElementAt(getOffset(text, position.line, position.character))
        return emptyList()
    }

    private fun getOffset(text: String, row: Int, col: Int): Int {
        return text.lineSequence().take(row).fold(0) { acc, l -> acc + l.length } + col
    }
}
