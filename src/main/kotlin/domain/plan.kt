package xyz.nietongxue.cfood.domain

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import kotlin.collections.set
import kotlin.time.Duration.Companion.minutes


data class Plan(val operations: List<Operation>) {
}

class PlanExecution(val plan: Plan) {
    val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)!!
    val operations = mutableListOf<Operation>().also {
        it.addAll(
            plan.operations
        )
    }
    val states = mutableMapOf<Id, OperationExecutionState>().also {
        it.putAll(plan.operations.map { it.id to OperationExecutionState.Waiting })

    }

    fun dispatch(station: Station): Operation? {
        val operation = operations.firstOrNull {
            states[it.id] == OperationExecutionState.Waiting
        }
        return operation?.let {
            states[it.id] = OperationExecutionState.Assigned
            it
        }
    }

    fun finish(operation: Operation) {
        states[operation.id] = OperationExecutionState.Finished
    }


}
typealias OperationAndState = Pair<Operation, OperationExecutionState>

interface OperationExecutionState {
    data object Waiting : OperationExecutionState
    data object Assigned : OperationExecutionState
    data object Finished : OperationExecutionState
}
