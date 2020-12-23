package suive.kotlinls.task

import suive.kotlinls.model.Params

interface NotificationTask<P : Params> : Task<List<P>> {
    fun method(): String
}
