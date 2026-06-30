package xyz.nietongxue.cfood

import xyz.nietongxue.cfood.domain.BOMService
import xyz.nietongxue.cfood.domain.LogisticService
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.OrchestrateService
import xyz.nietongxue.cfood.domain.OrderService
import xyz.nietongxue.cfood.domain.ProductService
import xyz.nietongxue.cfood.domain.RoutingService
import xyz.nietongxue.cfood.domain.World
import xyz.nietongxue.cfood.domain.path.LocalMap

class Facade {
    val productService = ProductService()
    val objectService = ObjectService(productService = productService)
    val orderService = OrderService(
        productService = productService,
        routingService = RoutingService(),
        bomService = BOMService(productService = productService)
    )
    val orchestrateService = OrchestrateService(
        productService = productService,
        routingService = RoutingService(),
        bomService = BOMService(productService = productService),
        orderService = orderService
    )

    val world = World(
        logisticService = LogisticService(),
        objectService = objectService,
        orchestrateService = orchestrateService,
        orderService = orderService,
        localMap = LocalMap(10, 10),
    )

    init {

        orderService.listener = orchestrateService
        Thread({
            while (true) {
                world.tick()
                Thread.sleep(100)
            }
        }).start()
    }
}