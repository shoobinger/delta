package suive.service

import suive.model.Diagnostic
import suive.model.Position
import suive.model.Range

class DiagnosticService {

    fun perform(fileContents: String): List<Diagnostic> {
        // TODO Call to the Kotlin compiler should be here.
        return listOf(
            Diagnostic(
                range = Range(
                    start = Position(line = 3, character = 13),
                    end = Position(line = 3, character = 13)
                ),
                message = "unresolved reference: invalidSymbol"
            )
        )
    }
}
