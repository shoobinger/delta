package suive.delta.model

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class CompletionOptions(
    val triggerCharacters: List<String> = listOf(".")
)

data class CompletionRegistrationOptions(
    @get: JsonUnwrapped
    val textDocumentRegistrationOptions: TextDocumentRegistrationOptions = TextDocumentRegistrationOptions(),
    @get: JsonUnwrapped
    val completionOptions: CompletionOptions = CompletionOptions()
)
