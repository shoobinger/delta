package suive.kotlinls.method

import suive.kotlinls.model.CompletionItem
import suive.kotlinls.model.CompletionParams
import suive.kotlinls.model.CompletionResult
import suive.kotlinls.service.CompletionService
import java.nio.file.Paths

class CompletionMethod(private val completionService: CompletionService) :
    Method<CompletionParams, CompletionResult>() {
    override fun doProcess(request: Request, params: CompletionParams): CompletionResult {
        val completions = completionService.getCompletions(Paths.get(params.textDocument.uri), params.position)
        return CompletionResult(request, items = completions.map { CompletionItem(label = it) })
    }
}
