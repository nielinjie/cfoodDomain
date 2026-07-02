package xyz.nietongxue.cfood.domain

import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.LocalMap

class World(
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val orchestrateService: OrchestrateService,
    val orderService: OrderService,
    var localMap: GameMap? = null,
) {

    val carriers = mutableListOf<Carrier>()
    val stations = mutableListOf<Station>()

    //    fun init() {
//        carriers.add(
//            Carrier(
//                objectService = objectService,
//                logisticService = logisticService,
//                localMap = localMap
//            ).also {
//                it.init()
//            }
//        )
//        stations.add(
//            Stove(
//                name = "main",
//                orchestrateService = orchestrateService,
//                objectService = objectService,
//                logisticService = logisticService,
//                world = this
//            ).also {
//                it.location = Location.XY(5, 5)
//                it.init()
//            },
//        )
//        stations.add(
//            Counter(
//                name = "counter",
//                location = Location.XY(0, 0),
//                objectService = objectService,
//                orderService = orderService
//            ).also {
//                it.init()
//            }
//        )
//    }
    fun setupFromDeclare(worldDeclare: WorldDeclare) {
        val localMap = LocalMap(worldDeclare.map.width, worldDeclare.map.height)
        this.localMap = localMap
        worldDeclare.stations.forEach {
            when (it.type) {
                "Stove" -> {
                    stations.add(
                        Stove(
                            name = it.name,
                            orchestrateService = orchestrateService,
                            objectService = objectService,
                            logisticService = logisticService,
                            world = this
                        ).also { s ->
                            s.location = Location.XY(it.location.x, it.location.y)
                            s.init()
                        }
                    )
                }

                "Counter" -> {
                    stations.add(
                        Counter(
                            name = it.name,
                            location = Location.XY(it.location.x, it.location.y),
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
            carriers.add(
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
        return stations.firstOrNull { it.name == name }
    }

    fun tick() {
        carriers.forEach {
            it.tick()
        }
        stations.forEach {
            it.tick()
        }
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
)

data class CarrierDeclare(
    val location: LocationDeclare,
)


data class LocationDeclare(
    val x: Int,
    val y: Int,
)