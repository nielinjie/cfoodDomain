package xyz.nietongxue.cfood.domain

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import kotlin.test.Test

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class BOMTest(
    val bomService: BOMService,
    val productService: ProductService
) {

    val egg = Product(name = "鸡蛋", code = "EGG", type = ProductType.RAW)

    //番茄
    val tomato = Product(name = "番茄", code = "TOMATO", type = ProductType.RAW)

    //番茄炒鸡蛋
    val tomatoEgg = Product(name = "番茄鸡蛋", code = "TOMATO_EGG", type = ProductType.FINISHED)
    val tomatoEggBom = BOM(
        productId = tomatoEgg.id, lines = listOf(
            BOMLine(componentId = tomato.id, quantity = 1),
            BOMLine(componentId = egg.id, quantity = 1)
        )
    )

    @Test
    fun test() {
        productService.save(egg)
        productService.save(tomato)
        productService.save(tomatoEgg)
        bomService.save(tomatoEggBom)
    }
}