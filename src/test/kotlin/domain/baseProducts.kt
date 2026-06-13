package xyz.nietongxue.cfood.domain

import kotlin.time.Duration.Companion.minutes

abstract class BaseProducts(
    val productService: ProductService,
    val routingService: RoutingService,
    val bomService: BOMService
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

    val tomatoEggOperation = RoutingOperation(
        code = "TOMATO_EGG_OP",
        name = "番茄鸡蛋",
        time = 2.minutes,
        productId = tomatoEgg.id, actionDescription = ""
    )
    val tomatoEggRouting = Routing(
        productId = tomatoEgg.id,
        lines = listOf(
            RoutingLine(operation = tomatoEggOperation, quantity = 1),
        )
    )

    fun setupProducts() {
        productService.save(egg)
        productService.save(tomato)
        productService.save(tomatoEgg)
        routingService.save(tomatoEggRouting)
        bomService.save(tomatoEggBom)
    }
}