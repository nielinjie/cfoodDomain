package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7


data class LogisticTask(
    override val id: Id = v7(),
    val productId: Id,
    val quantity: Int,
    val dest: Location
) : HasId

interface LogisticTaskState {
    object Waiting : LogisticTaskState
    data class Processing(val carryId: Id, val objectIds: List<Id>) : LogisticTaskState
    object Finished : LogisticTaskState
}

@Service
class LogisticService {

    val logisticTasks = mutableListOf<LogisticTask>()
    val states = mutableMapOf<Id, LogisticTaskState>()
    fun logisticRequest(productId: String, quantity: Int, dest: Location) {
        val task = LogisticTask(productId = productId, quantity = quantity, dest = dest)
        logisticTasks.add(task)
        states[task.id] = LogisticTaskState.Waiting
    }

    fun dispatch(carryId: Id): LogisticTask? {
        return logisticTasks.firstOrNull { task ->
            states[task.id] == LogisticTaskState.Waiting
        }?.also { task ->
            states[task.id] = LogisticTaskState.Processing(carryId, listOf())
        }
    }

    fun finish(task: LogisticTask) {
        states[task.id] = LogisticTaskState.Finished
    }
}

