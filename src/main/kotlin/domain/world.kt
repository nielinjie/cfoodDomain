package xyz.nietongxue.cfood.domain

import xyz.nietongxue.cfood.domain.path.GameMap

class World(
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val orchestrateService: OrchestrateService,
    val orderService: OrderService,
    val localMap: GameMap,
) {

    val carriers = mutableListOf<Carrier>()
    val stations = mutableListOf<Station>()

    fun init() {
        carriers.add(
            Carrier(
                objectService = objectService,
                logisticService = logisticService,
                localMap = localMap
            ).also {
                it.init()
            }
        )
        stations.add(
            Stove(
                name = "main",
                orchestrateService = orchestrateService,
                objectService = objectService,
                logisticService = logisticService,
                world = this
            ).also {
                it.location = Location.XY(5, 5)
                it.init()
            },
        )
        stations.add(
            Counter(
                name = "counter",
                location = Location.XY(0, 0),
                objectService = objectService,
                orderService = orderService
            ).also {
                it.init()
            }
        )
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