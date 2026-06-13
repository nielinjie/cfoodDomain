package xyz.nietongxue.cfood.domain

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes


data class Plan(val operations: List<Operation>)

@Service
class OrchestrateService(
    val productService: ProductService,
    val routingService: RoutingService,
    val bomService: BOMService,
    val orderService: OrderService
) {
    val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    var plan: Plan? = null
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
    }
}

