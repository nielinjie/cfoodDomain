package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId

interface Task : HasId

interface TaskManager {
    fun dispatch(acceptId: String): Task?
    fun finish(task: Task)
}