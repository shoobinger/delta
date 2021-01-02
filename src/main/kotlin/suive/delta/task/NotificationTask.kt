package suive.delta.task

import suive.delta.model.Params

interface NotificationTask<P : Params> : Task<List<P>> {
    fun method(): String
}
