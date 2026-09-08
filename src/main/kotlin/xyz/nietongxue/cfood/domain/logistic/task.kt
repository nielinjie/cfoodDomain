package xyz.nietongxue.cfood.domain.logistic

import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.Task
import xyz.nietongxue.cfood.domain.TaskState
import xyz.nietongxue.cfood.domain.station.Station
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7


interface LogisticTask : Task

data class LogisticUnloadTask(
    override val id: Id = v7(), val productId: Id, val quantity: Int, val dest: Location
) : LogisticTask


data class LogisticTransformTask(
    override val id: Id = v7(), val productId: Id, val quantity: Int, val destStation: Station
) : LogisticTask

data class ProcessingLogistic(val carryId: Id, val objectIds: List<Id>) : TaskState
