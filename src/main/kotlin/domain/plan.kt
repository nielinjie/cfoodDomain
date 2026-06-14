package xyz.nietongxue.cfood.domain

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes


data class Plan(val operations: List<Operation>) {
}

class PlanExecution(val plan: Plan) {
    val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)!!
    val operations = mutableListOf<OperationAndState>().also {
        it.addAll(
            plan.operations.map { it to OperationExecutionState.Waiting }
        )
    }

    fun dispatch(station: Station): Operation? {
        val operation = operations.firstOrNull { it.second == OperationExecutionState.Waiting }
        return operation?.first?.also {
            changeState(operation.first, OperationExecutionState.Assigned(station))
        }
    }

    fun finish(operation: Operation) {
        changeState(operation, OperationExecutionState.Finished)
    }

    fun changeState(operation: Operation, newState: OperationExecutionState) {
        operations.removeIf { it.first == operation }
        operations.add(operation to newState)
    }
}
typealias OperationAndState = Pair<Operation, OperationExecutionState>

interface OperationExecutionState {
    data object Waiting : OperationExecutionState
    data class Assigned(val station: Station) : OperationExecutionState
    data object Finished : OperationExecutionState
}

@Service
class OrchestrateService(
    val productService: ProductService,
    val routingService: RoutingService,
    val bomService: BOMService,
    val orderService: OrderService
) {
    val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)!!

    var plan: Plan? = null
    var execution: PlanExecution? = null
    fun plan(): Plan {
        val order = orderService.getWaiting().firstOrNull() ?: error("no order")
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
                            time = 1.minutes,
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
        plan = plan()
        execution = PlanExecution(plan!!)
    }
}

