package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId



interface TaskWatching {
    fun pull()
}


interface TaskWatcher {
    val taskWatchingList: List<TaskWatching>

    fun tock() {
        taskWatchingList.forEach {
            it.pull()
        }
    }
}

