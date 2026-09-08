package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId

interface Task : HasId

interface TaskManager {
    /**
     * 任务分配给xxx。
     */
    fun dispatch(acceptId: String): Task?

    /**
     * 任务完成，交还。
     */
    fun finish(task: Task)
}

interface TaskState {
    object Waiting : TaskState
    object Finished : TaskState
}


data class TaskStateUpdate(val taskId: String) : Action


data object CheckTaskAction : Action