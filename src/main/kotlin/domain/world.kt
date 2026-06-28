package xyz.nietongxue.cfood.domain

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.path.GameMap

@Service
class World(
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val orchestrateService: OrchestrateService,
    val localMap: GameMap,
) {

    val carriers = mutableListOf<Carrier>()
    val stations = mutableListOf<Station>()

    @PostConstruct
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
            ).also {
                it.location = Location.XY(5, 5)
                it.init()
            }
        )
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