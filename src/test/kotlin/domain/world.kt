package xyz.nietongxue.cfood.domain

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import xyz.nietongxue.cfood.Main
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.LocalMap
import java.time.LocalDateTime
import kotlin.test.Test

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = [Main::class])
@TestConstructor(autowireMode = AutowireMode.ALL)
class WorldTest(
    val world: World,
    val objectService: ObjectService,
    val logisticService: LogisticService,
    productService: ProductService,
    routingService: RoutingService,
    bomService: BOMService,
    val orderService: OrderService
) : BaseProducts(productService, routingService, bomService) {


    @TestConfiguration
    class Configure() {
        @Bean
        fun localMap(): GameMap {
            return LocalMap(10, 10)
        }
    }


    @Test
    fun stationTest() {
        setupProducts()
        val order = Order(
            code = "TOMATO_EGG_ORDER",
            lines = listOf(
                OrderLine(productCode = tomatoEgg.id, quantity = 1)
            ),
            requiredTime = LocalDateTime.now().plusHours(12),
            state = OrderState.Waiting
        )
        orderService.accept(order)
        objectService.input(this.tomato.id, 10, Location.XY(3, 5))
        objectService.input(this.egg.id, 10, Location.XY(5, 7))
        for (i in 1..60) {
            world.tick()
            Thread.sleep(100)
        }
        objectService.objects.count().also {
            println("objects count - $it")
        }
    }

    @Test
    fun carrierTest() {

        objectService.input(this.egg.id, 10, location = Location.XY(2, 4))
        logisticService.logisticRequest(this.egg.id, 1, Location.XY(6, 1))
        for (i in 1..50) {
            world.tick()
            Thread.sleep(100)
        }
    }
}