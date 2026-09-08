package xyz.nietongxue.cfood.domain.staff

import xyz.nietongxue.cfood.domain.ActCapability
import xyz.nietongxue.cfood.domain.Action
import xyz.nietongxue.cfood.domain.Actor
import xyz.nietongxue.cfood.domain.Carriable
import xyz.nietongxue.cfood.domain.CarryCapability
import xyz.nietongxue.cfood.domain.CheckTaskAction
import xyz.nietongxue.cfood.domain.Direction
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.logistic.LogisticTask
import xyz.nietongxue.cfood.domain.logistic.LogisticTaskHandle
import xyz.nietongxue.cfood.domain.logistic.LogisticTaskWorkerCapability
import xyz.nietongxue.cfood.domain.Movable
import xyz.nietongxue.cfood.domain.MoveCapability
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.Speed
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7

/**
 * 侍者
 * 基本上就是搬运工。
 */

class Waiter(
    val objectService: ObjectService,
    val logisticService: LogisticService,
    override val id: Id = v7(),
    val localMap: GameMap,
) : Actor, Movable, Carriable, LogisticTaskHandle {

    fun init() {
        queue.add(CheckTaskAction)
    }


    override var location: Location = Location.XY(0, 0)
    override var maxSpeed: Int = 1
    override var carrying: String? = null
    override var task: LogisticTask? = null
    override var speed: Speed = Speed(Direction.S, 0)

    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()


    override val actCapabilities: List<ActCapability> = listOf(
        MoveCapability(this, objectService),
        CarryCapability(this, objectService),
        LogisticTaskWorkerCapability(this, logisticService, objectService, localMap)
    )


}