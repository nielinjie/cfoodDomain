package xyz.nietongxue.cfood.domain

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7


class Carrier(
    val objectService: ObjectService,
    val logisticService: LogisticService,
    override val id: Id = v7(),
    val localMap: GameMap,
) : Actor, Movable, Carriable, LogisticTaskHandle {

    fun init() {
        queue.add(CheckTaskAction)
    }

    val logger = LoggerFactory.getLogger(this::class.java)!!

    override var location: Location = Location.XY(0, 0)
    override var speed: Int = 1
    override var carrying: String? = null
    override var task: LogisticTask? = null


    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()


    override val actCapabilities: List<ActCapability> = listOf(
        MoveCapability(this, objectService),
        CarryCapability(this, objectService),
        LogisticTaskWorkerCapability(this, logisticService, objectService, localMap)
    )


}
