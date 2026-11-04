package xyz.nietongxue.cfood.domain.staff

import xyz.nietongxue.cfood.domain.CheckTaskAction
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.action.Ability
import xyz.nietongxue.cfood.domain.action.Actor
import xyz.nietongxue.cfood.domain.carry.Carriable
import xyz.nietongxue.cfood.domain.carry.CarryAbility
import xyz.nietongxue.cfood.domain.logistic.LogisticAbility
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.logistic.LogisticTask
import xyz.nietongxue.cfood.domain.logistic.TaskHandle
import xyz.nietongxue.cfood.domain.map.GameMap
import xyz.nietongxue.cfood.domain.move.Movable
import xyz.nietongxue.cfood.domain.move.MoveAbility
import xyz.nietongxue.cfood.domain.move.Speed
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7

/**
 * 侍者
 * 基本上就是搬运工。
 */

class Waiter(
    val objectService: ObjectService,
    val logisticService: LogisticService,
    id: Id = v7(),
    val localMap: GameMap,
) : Actor(id), Movable, Carriable, TaskHandle<LogisticTask>, Staff {

    fun init() {
        queue.add(CheckTaskAction)
    }


    override var location: Location = Location.XY(0, 0)
    override var maxSpeed: Int = 7
    override var carrying: String? = null
    override var task: LogisticTask? = null
    override var speed: Speed = Speed.zero


    override val abilities: List<Ability> = listOf(
        MoveAbility(this, objectService),
        CarryAbility(this, objectService),
        LogisticAbility(this, logisticService, objectService, localMap)
    )


}