package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import java.time.LocalDateTime


@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class OperationTest(
    val orderService: OrderService,
    val orchestrateService: OrchestrateService,
    productService: ProductService,
    routingService: RoutingService,
    bomService: BOMService,
) : BaseProducts(
    productService, routingService, bomService
) {


    @Test
    fun operations() {
        setupProducts()
        val order = Order(
            code = "TOMATO_EGG_ORDER",
            lines = listOf(
                OrderLine(productCode = "TOMATO_EGG", quantity = 1)
            ),
            requiredTime = LocalDateTime.now().plusHours(12),
            state = OrderState.Waiting
        )
        orderService.accept(order)
        println("orders size: ${orderService.orders.size}")
        println("orders state: ${orderService.orders.map { it.state }}")
        println("getWaiting size: ${orderService.getWaiting().size}")
        val plan = orchestrateService.plan
        val execution = orchestrateService.execution
        println(plan)
        println(execution)
        println(execution?.operations)
    }
}