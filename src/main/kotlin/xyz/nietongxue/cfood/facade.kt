package xyz.nietongxue.cfood

import com.fasterxml.jackson.module.kotlin.readValue
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.path.LocalMap
import xyz.nietongxue.common.json.defaultOM

class Facade() {
    val productService = ProductService()
    val routingService = RoutingService()
    val bomService = BOMService(productService = productService)
    val objectService = ObjectService(productService = productService)
    val logisticService = LogisticService()
    val localMap = LocalMap(10, 10)
    val orderService = OrderService(
        productService = productService,
        routingService = routingService,
        bomService = bomService
    )
    val orchestrateService = OrchestrateService(
        productService = productService,
        routingService = routingService,
        bomService = bomService,
        orderService = orderService
    ).also {
        orderService.listener = it
    }

    val world = World(
        logisticService = logisticService,
        objectService = objectService,
        orchestrateService = orchestrateService,
        orderService = orderService,
        localMap = localMap,
    ).also {
//        it.init()
    }





    val thread = Thread {
        while (true) {
            world.tick()
            runCatching {  Thread.sleep(100)}
        }
    }

    fun start() {
        thread.start()
    }

    init {
        orderService.listener = orchestrateService
    }


    fun setupProducts(json: String) {
        val tree = defaultOM.readTree(json)
        val products = tree["products"].map {
            defaultOM.treeToValue(it, Product::class.java)
        }
        val routings = tree["routings"].map {
            defaultOM.treeToValue(it, Routing::class.java)
        }
        val bom = tree["boms"].map {
            defaultOM.treeToValue(it, BOM::class.java)
        }
        productService.products.clear()
        productService.products.addAll(products)
        routingService.routingList.clear()
        routingService.routingList.addAll(routings)
        bomService.boms.clear()
        bomService.boms.addAll(bom)

    }

    fun setupMap(json: String) {
        val world = defaultOM.readValue<WorldDeclare>(json)
        this.world.setupFromDeclare(world)
    }

    fun stop() {
        this.thread.interrupt()
    }
}