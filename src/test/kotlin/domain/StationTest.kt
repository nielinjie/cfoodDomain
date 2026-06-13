package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import java.time.LocalDateTime

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class StationTest(
    val orderService: OrderService,
    productService: ProductService,
    routingService: RoutingService,
    bomService: BOMService,
) : BaseProducts(
    productService, routingService, bomService
) {


    @Test
    fun dispatch() {
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
    }
}