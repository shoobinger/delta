package suive.service

import suive.model.Diagnostic
import suive.model.Position
import suive.model.Range

class DiagnosticService {

    fun perform(fileContents: String): Diagnostic {
        // TODO Call to the Kotlin compiler should be here.
        return Diagnostic(
            range = Range(
                start = Position(line = 3u, character = 13u),
                end = Position(line = 3u, character = 13u)
            ),
            message = "unresolved reference: invalidSymbol"
        )
    }
}
