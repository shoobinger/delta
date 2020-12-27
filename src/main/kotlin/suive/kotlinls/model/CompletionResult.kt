package suive.kotlinls.model

import suive.kotlinls.method.Request

data class CompletionResult(
    override val request: Request,
    val isIncomplete: Boolean = false,
    val items: List<CompletionItem>
) : Output.Result(request)

data class CompletionItem(
    val label: String,
    val kind: Int = 1
)
