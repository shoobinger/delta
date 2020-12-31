package suive.kotlinls.task

import suive.kotlinls.Workspace
import suive.kotlinls.model.DidChangeTextDocumentParams

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
