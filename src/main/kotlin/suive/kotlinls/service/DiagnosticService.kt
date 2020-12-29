package suive.kotlinls.service

import suive.kotlinls.model.Diagnostic
import suive.kotlinls.model.Position
import suive.kotlinls.model.Range

class DiagnosticService(
    private val compilerService: CompilerService
) {

    fun perform(fileContents: String): List<Diagnostic> {
        // TODO Call to the Kotlin compiler should be here.
        return listOf(

        )
    }
}
