package xyz.nietongxue.cfood.domain.staff

import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.logistic.LogisticAbility
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.logistic.LogisticTask
import xyz.nietongxue.cfood.domain.logistic.TaskHandle
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
    id: Id = v7(),
    val localMap: GameMap,
) : Actor(id), Movable, Carriable, TaskHandle<LogisticTask> {

    fun init() {
        queue.add(CheckTaskAction)
    }


    override var location: Location = Location.XY(0, 0)
    override var maxSpeed: Int = 1
    override var carrying: String? = null
    override var task: LogisticTask? = null
    override var speed: Speed = Speed(Direction.S, 0)



    override val abilities: List<Ability> = listOf(
        MoveAbility(this, objectService),
        CarryAbility(this, objectService),
        LogisticAbility(this, logisticService, objectService, localMap)
    )


}