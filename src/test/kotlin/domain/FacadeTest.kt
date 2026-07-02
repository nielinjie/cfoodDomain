package xyz.nietongxue.cfood.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import xyz.nietongxue.cfood.Facade
import xyz.nietongxue.common.collections.toJsonString
import xyz.nietongxue.common.json.defaultOM
import xyz.nietongxue.common.json.ja
import xyz.nietongxue.common.json.jo
import java.time.LocalDateTime
import kotlin.test.Test

class FacadeTest {


    @Test
    fun product() {
        val facade = Facade()
        with(facade) {
            val products = productService.products.map { defaultOM.valueToTree<JsonNode>(it) }
            val routings = routingService.routingList.map { defaultOM.valueToTree<JsonNode>(it) }
            val bom = bomService.boms.map { defaultOM.valueToTree<JsonNode>(it) }
            jo {
                put("products", ja(*products.toTypedArray()))
                put("routings", ja(*routings.toTypedArray()))
                put("boms", ja(*bom.toTypedArray()))
            }.also {
                println(it.toPrettyString())
            }
        }
    }


    @Test
    fun loadProducts() {
        val string = (javaClass.getResource("/products.json")!!.readText())
        val tree = defaultOM.readTree(string)
        val products = tree["products"].map {
            val product = defaultOM.treeToValue(it, Product::class.java)
//            productService.save(product)
            product
        }
        val routings = tree["routings"].map {
            val routing = defaultOM.treeToValue(it, Routing::class.java)
//            routingService.save(routing)
            routing
        }
        val bom = tree["boms"].map {
            val bom = defaultOM.treeToValue(it, BOM::class.java)
//            bomService.save(bom)
            bom
        }
        val facade = Facade()
        with(facade) {
            productService.products.clear()
            productService.products.addAll(products)
            routingService.routingList.clear()
            routingService.routingList.addAll(routings)
            bomService.boms.clear()
            bomService.boms.addAll(bom)
        }
    }


    @Test
    fun mapSave() {
        val facade = Facade()
        with(facade) {
            val map = facade.localMap.let {
                val de = GameMapDeclare(
                    width = it.width,
                    height = it.height
                )
                de
            }
            val stations = facade.world.stations.map {
                val de = StationDeclare(
                    name = it.name,
                    type = when (it) {
                        is Stove -> "Stove"
                        is Counter -> "Counter"
                        else -> error("no such type")
                    },
                    location = LocationDeclare(
                        x = it.location.position().x,
                        y = it.location.position().y
                    )
                )
                de
            }
            val carriers = facade.world.carriers.map {
                val de = CarrierDeclare(
                    location = LocationDeclare(
                        x = it.location.position().x,
                        y = it.location.position().y
                    ),
                )
                de
            }
            WorldDeclare(stations = stations, carriers = carriers, map = map).also {
                println(it.toJsonString(true))
            }
        }
    }


    @Test
    fun loadMap() {
        val facade = Facade()
        val string = (javaClass.getResource("/map.json").readText())
        val world = defaultOM.readValue<WorldDeclare>(string)
        println(world)
        facade.world.setupFromDeclare(world)
        facade.world.also {
            println(it)
        }

    }

    @Test
    fun setupTest() {
        val facade = Facade()
        facade.setupMap((javaClass.getResource("/map.json")!!.readText()))
        facade.setupProducts((javaClass.getResource("/products.json")!!.readText()))

        with(facade) {
            val order = Order(
                code = "TOMATO_EGG_ORDER",
                lines = listOf(
                    OrderLine(productCode = "TOMATO_EGG", quantity = 3)
                ),
                requiredTime = LocalDateTime.now().plusHours(12),
                state = OrderState.Waiting
            )
            orderService.accept(order)
            objectService.inputByCode("TOMATO", 10, Location.XY(3, 5))
            objectService.inputByCode("EGG", 10, Location.XY(5, 7))
        }
        facade.start()

        Thread.sleep(10000)
    }

}