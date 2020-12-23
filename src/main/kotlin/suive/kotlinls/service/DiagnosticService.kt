package suive.kotlinls.service

import suive.kotlinls.model.Diagnostic
import suive.kotlinls.model.Position
import suive.kotlinls.model.Range

class DiagnosticService {

    fun perform(fileContents: String): List<Diagnostic> {
        // TODO Call to the Kotlin compiler should be here.
        return listOf(
            Diagnostic(
                range =Range(
                    start = Position(line = 3, character = 13),
                    end = Position(line = 3, character = 13)
                ),
                message = "unresolved reference: invalidSymbol"
            )
        )
    }
}
