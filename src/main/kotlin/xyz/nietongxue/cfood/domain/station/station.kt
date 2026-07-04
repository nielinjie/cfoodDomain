package xyz.nietongxue.cfood.domain.station

import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.facility.Facility
import xyz.nietongxue.common.base.Id


class OperationTask(override val id: Id, val operation: Operation) : Task

interface Station : Actor, Facility //有事情要干的设施是station，工作站

data class ProcessingOperation(val stationId: Id) : TaskState

class OperationAction(val operation: Operation, var progress: Int, val startedTime: Long) : Action
class CheckInputAction() : Action
class RequestInputAction() : Action




