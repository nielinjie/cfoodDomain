package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.LocalMap
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
    val objectService: ObjectService
) : BaseProducts(
    productService, routingService, bomService
) {

    @Autowired
    lateinit var world: World

    @TestConfiguration
    class Configure() {
        @Bean
        fun localMap(): GameMap {
            return LocalMap(10, 10)
        }
    }

    @Test
    fun dispatch() {
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
        val stove = Stove(
            name = "main",
            orchestrateService = orchestrateService,
            logisticService = logisticService,
            objectService = objectService,
            world = world
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
                OrderLine(productCode = "TOMATO_EGG", quantity = 1)
            ),
            requiredTime = LocalDateTime.now().plusHours(12),
            state = OrderState.Waiting
        )
        orderService.accept(order)
        val stove = Stove(
            name = "main",
            orchestrateService = orchestrateService,
            logisticService = logisticService,
            objectService = objectService,
            world = world
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
    fun tasks() {
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

        for (i in 1..30) {
            world.tick()
            Thread.sleep(100)
        }
    }
}