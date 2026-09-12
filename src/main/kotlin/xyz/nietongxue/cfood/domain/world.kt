package xyz.nietongxue.cfood.domain

import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import xyz.nietongxue.cfood.domain.action.Actor
import xyz.nietongxue.cfood.domain.facility.HouseService
import xyz.nietongxue.cfood.domain.facility.Port
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.LocalMap
import xyz.nietongxue.cfood.domain.staff.Carrier
import xyz.nietongxue.cfood.domain.staff.StaffService
import xyz.nietongxue.cfood.domain.station.Counter
import xyz.nietongxue.cfood.domain.station.Station
import xyz.nietongxue.cfood.domain.station.Stove
import xyz.nietongxue.common.json.defaultOM


@Component
class World(
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val orchestrateService: OrchestrateService,
    val orderService: OrderService,
    val productService: ProductService,
    val bomService: BOMService,
    val routingService: RoutingService,
    val staffService: StaffService,
    val houseService: HouseService,
    var localMap: GameMap? = null,
) {

    init {
        orderService.listener = orchestrateService
    }


    fun setupFromDeclare(worldDeclare: WorldDeclare) {
        val localMap = LocalMap(worldDeclare.map.width, worldDeclare.map.height)
        this.localMap = localMap
        worldDeclare.stations.forEach {
            when (it.type) {
                "Stove" -> {
                    houseService.facilities.add(
                        Stove(
                            name = it.name,
                            orchestrateService = orchestrateService,
                            objectService = objectService,
                            logisticService = logisticService,
                            world = this,
                            size = it.size,
                            ports = it.ports,
                            location = Location.XY(it.location.x, it.location.y)
                        ).also { s ->
                            s.init()
                        }
                    )
                }

                "Counter" -> {
                    houseService.facilities.add(
                        Counter(
                            name = it.name,
                            location = Location.XY(it.location.x, it.location.y),
                            size = it.size,
                            ports = it.ports,
                            objectService = objectService,
                            orderService = orderService,
                            productService = objectService.productService
                        ).also {
                            it.init()
                        }
                    )
                }
            }
        }
        worldDeclare.carriers.forEach {
            staffService.staff.add(
                Carrier(
                    objectService = objectService,
                    logisticService = logisticService,
                    localMap = localMap
                ).also { c ->
                    c.location = Location.XY(it.location.x, it.location.y)
                    c.init()
                }
            )
        }
    }

    fun getStation(name: String): Station? {
        return houseService.facilities.filterIsInstance<Station>().firstOrNull { it.name == name }
    }

    fun tick() {
        staffService.staff.forEach {
            (it as? Actor)?.tick()
        }
        houseService.facilities.forEach {
            (it as? Actor)?.tick()
        }
    }

    fun setupMap(json: String) {
        val world = defaultOM.readValue<WorldDeclare>(json)
        this.setupFromDeclare(world)
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


}

data class WorldDeclare(
    val stations: List<StationDeclare>,
    val carriers: List<CarrierDeclare>,
    val map: GameMapDeclare
)


data class GameMapDeclare(
    val width: Int,
    val height: Int,
)

data class StationDeclare(
    val name: String,
    val type: String,
    val location: LocationDeclare,
    val size: Size,
    val ports: List<Port>,
)

data class CarrierDeclare(
    val location: LocationDeclare,
)


data class LocationDeclare(
    val x: Int,
    val y: Int,
)