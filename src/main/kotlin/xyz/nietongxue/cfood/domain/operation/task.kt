package xyz.nietongxue.cfood.domain.operation

import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.Task
import xyz.nietongxue.cfood.domain.TaskState
import xyz.nietongxue.common.base.Id

class OperationTask(override val id: Id, val operation: Operation) : Task
data class ProcessingOperation(val stationId: Id) : TaskState
class OperationAction(val operation: Operation, var progress: Int, val startedTime: Long) : Action
class CheckInputAction() : Action
class RequestInputAction() : Action


interface OperationTaskHandle {
    var task: OperationTask?
}