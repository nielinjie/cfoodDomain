package xyz.nietongxue.cfood.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xyz.nietongxue.cfood.Universe
import xyz.nietongxue.common.json.defaultOM
import xyz.nietongxue.common.json.ja
import xyz.nietongxue.common.json.jo
import java.time.LocalDateTime


@SpringBootTest
class UniverseTest {

    @Autowired
    lateinit var universe: Universe

    @Test
    fun product() {
        with(universe.world) {
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
        with(universe.world) {
            productService.products.clear()
            productService.products.addAll(products)
            routingService.routingList.clear()
            routingService.routingList.addAll(routings)
            bomService.boms.clear()
            bomService.boms.addAll(bom)
        }
    }


    @Test
    fun loadMap() {
        val string = (javaClass.getResource("/map.json")!!.readText())
        val world = defaultOM.readValue<WorldDeclare>(string)
        println(world)
        universe.world.setupFromDeclare(world)
        universe.world.also {
            println(it)
        }

    }

    @Test
    fun setupTest() {

        universe.start()
        Thread.sleep(2000)
        with(universe.world) {
            val order = Order(
                code = "TOMATO_EGG_ORDER",
                lines = listOf(
                    OrderLine(productCode = "TOMATO_EGG", quantity = 3)
                ),
                requiredTime = LocalDateTime.now().plusHours(12),
                state = OrderState.Waiting
            )
            orderService.accept(order)
            objectService.inputByCode("TOMATO", 10, Location.XY(30, 50))
            objectService.inputByCode("EGG", 10, Location.XY(50, 70))
        }

        Thread.sleep(10000)
        universe.stop()
    }

}