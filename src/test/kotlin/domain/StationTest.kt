package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import xyz.nietongxue.common.base.v7
import java.time.LocalDateTime

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class StationTest(
    val orderService: OrderService,
    productService: ProductService,
    routingService: RoutingService,
    bomService: BOMService,
    val orchestrateService: OrchestrateService,
    val logisticService: LogisticService,
    val applicationContext: ApplicationContext
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
        val stove = Stove(
//            name = "main",
            id = v7(),
            orchestrateService
        )
        val operation = orchestrateService.execution!!.dispatch(stove)!!
        println(operation)
        println(orchestrateService.execution!!.operations)
        orchestrateService.execution!!.finish(operation)
        println(orchestrateService.execution!!.operations)

    }

    @Test
    fun logistic() {
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
        val stove = Stove(
//            name = "main",
            id = v7(),
            orchestrateService
        )
        val operation = orchestrateService.execution!!.dispatch(stove)!!
        val consume = operation.consume.groupBy { it.product.id }.map {
            it.key to it.value.sumOf { it.quantity }
        }
        consume.forEach {
            logisticService.logisticRequest(it.first, it.second, stove.location)
        }
        val task = logisticService.dispatch(v7())
        println(task)
        println(logisticService.tasks)
        logisticService.finish(task!!)
        println(logisticService.tasks)
    }

    @Test
    fun tasks(){
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
        val station = applicationContext.getBean<Stove>().also {
            it.location = Location.XY(5, 5)
        }
        for (i in 1..30) {
            station.tick()
            Thread.sleep(100)
        }
    }
}