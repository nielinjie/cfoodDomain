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

@Service
class OrchestrateService(
    val productService: ProductService,
    val routingService: RoutingService,
    val bomService: BOMService,
    val orderService: OrderService
) : TaskManager {
    val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)!!

    var plan: Plan? = null
    var execution: PlanExecution? = null
    val tasks = mutableListOf<OperationTask>()
    val states = mutableMapOf<Id, TaskState>()
    fun plan(): Plan {
        val order = orderService.getWaiting().firstOrNull() ?: error("no order") //TODO 没有处理遗留任务。
        return order.lines.flatMap {
            val product = it.productId
            val routing = routingService.getByProductId(product)!!
            val bom = bomService.getByProductId(product)
            val consume = bomService.getComponents(product)
            routing.lines.flatMap { line ->
                line.operation.let { routingOperation ->
                    listOf(
                        Operation(
                            code = routingOperation.code,
                            name = routingOperation.name,
                            time = routingOperation.time,
                            product = productService.getById(product)!!,
                            consume = consume.map { component ->
                                Consume(productService.getById(component.componentId)!!, component.quantity)
                            },
                        )
                    )
                }
            }
        }.let { Plan(it) }
    }


    @EventListener
    fun orderChanged(event: OrderEvent) {
        logger.info("order changed")
        plan = plan() //TODO 没有处理遗留任务。
        execution = PlanExecution(plan!!)
        tasksFromExecution().also {
            tasks.addAll(it)
            states.putAll(it.map { it.id to TaskState.Waiting })
        }
    }

    fun tasksFromExecution(): List<OperationTask> {
        return execution!!.operations.map { operation ->
            OperationTask(
                id = v7(),
                operation = operation
            )
        }
    }

    override fun dispatch(acceptId: Id): OperationTask? {
        return tasks.firstOrNull { task ->
            states[task.id] == TaskState.Waiting
        }?.also { task ->
            states[task.id] = ProcessingOperation(acceptId)
        }?.also {
            logger.info("dispatch task - $it")
        }
    }

    override fun finish(task: Task) {
        require(task is OperationTask)
        states[task.id] = TaskState.Finished
    }
}

