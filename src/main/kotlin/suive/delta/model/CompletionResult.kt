package suive.delta.model

data class CompletionResult(
    val isIncomplete: Boolean = false,
    val items: List<CompletionItem>
)

data class CompletionItem(
    val label: String,
    val kind: Int = 1
)
