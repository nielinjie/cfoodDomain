package xyz.nietongxue.cfood.domain

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import java.time.LocalDateTime
import kotlin.test.Test


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
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
                OrderLine(productId = tomatoEgg.id, quantity = 1)
            ),
            requiredTime = LocalDateTime.now().plusHours(12),
            status = OrderState.Waiting
        )
        orderService.accept(order)
        val plan = orchestrateService.plan
        val execution = orchestrateService.execution
        println(plan)
        println(execution)
        println(execution?.operations)
    }
}