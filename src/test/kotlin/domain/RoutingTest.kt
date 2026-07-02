package xyz.nietongxue.cfood.domain

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import java.time.Duration
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class RoutingTest(
    val productService: ProductService,
    val routingService: RoutingService
) {

    val egg = Product(name = "鸡蛋", code = "EGG", type = ProductType.RAW)

    //番茄
    val tomato = Product(name = "番茄", code = "TOMATO", type = ProductType.RAW)

    //番茄炒鸡蛋
    val tomatoEgg = Product(name = "番茄鸡蛋", code = "TOMATO_EGG", type = ProductType.FINISHED)
    val tomatoEggOperation = RoutingOperation(
        code = "TOMATO_EGG_OP",
        name = "番茄鸡蛋",
        time = Duration.ofMinutes(2),
        productId = tomatoEgg.id, actionDescription = ""
    )
    val tomatoEggRouting = Routing(
        productId = tomatoEgg.id,
        lines = listOf(
            RoutingLine(operation = tomatoEggOperation, quantity = 1),
        )
    )

    @Test
    fun test() {
        productService.save(egg)
        productService.save(tomato)
        productService.save(tomatoEgg)
        routingService.save(tomatoEggRouting)
    }
}