package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class MoveTest(
    productService: ProductService,
    routingService: RoutingService,
    bomService: BOMService,
    val objectService: ObjectService,
    val applicationContext: ApplicationContext
) : BaseProducts(
    productService, routingService, bomService
) {
    val logger = getLogger(this::class.java)!!


    @Test
    fun move() {
        val carrier = applicationContext.getBean<Carrier>()
        carrier.accept(
            MoveAction(Location.XY(3, 2)),
            LoadAction(Location.XY(3, 2), "obj1"),
            MoveAction(Location.XY(6, 1)),
            UnloadAction(Location.XY(6, 1)), TaskStateUpdate("task1")
        )

        for (i in 1..15) {
            carrier.tick()
            Thread.sleep(200)
        }
    }

    @Test
    fun moveByLogisticTask() {
        setupProducts()
        val carrier = applicationContext.getBean<Carrier>()
        objectService.input(this.egg.id, 10, location = Location.XY(2, 4))
        val logisticTask = LogisticTask(
            id = "task1",
            productId = this.egg.id,
            quantity = 1,
            dest = Location.XY(6, 1),
        )
        carrier.accept(
            logisticTask
        )

        for (i in 1..40) {
            carrier.tick()
            Thread.sleep(100)
        }
    }
}