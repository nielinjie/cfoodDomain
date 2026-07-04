package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId

interface Task : HasId

interface TaskManager {
    fun dispatch(acceptId: String): Task?
    fun finish(task: Task)
}

interface TaskState {
    object Waiting : TaskState
    object Finished : TaskState
}


data class TaskStateUpdate(val taskId: String) : Action


data object CheckTaskAction : Action