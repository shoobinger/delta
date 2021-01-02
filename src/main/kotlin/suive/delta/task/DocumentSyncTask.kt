package suive.delta.task

import suive.delta.Workspace
import suive.delta.model.DidChangeTextDocumentParams

class DocumentSyncTask(
    private val workspace: Workspace,
    private val params: DidChangeTextDocumentParams
) : Task<Unit> {
    override fun execute() {
        params.contentChanges.forEach { change ->
            workspace.enqueueChange(params.textDocument.uri, change)
        }
    }
}
