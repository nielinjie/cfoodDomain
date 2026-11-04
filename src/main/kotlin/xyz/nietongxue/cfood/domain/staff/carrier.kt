package xyz.nietongxue.cfood.domain.staff

import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.action.Ability
import xyz.nietongxue.cfood.domain.action.Actor
import xyz.nietongxue.cfood.domain.logistic.LogisticAbility
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.logistic.LogisticTask
import xyz.nietongxue.cfood.domain.logistic.TaskHandle
import xyz.nietongxue.cfood.domain.map.GameMap
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7

/**
 * 搬运工
 * 一般是在后台活动。
 * actionSeq -
 *  1. 检查物流任务
 *  2. （loop）
 *  3. 物流任务分解
 *      1. 找到 object
 *      2. 找到 path
 *      3. move to object
 *      4. carry
 *      5. move to dest
 *      6. drop/ transform
 */

class Carrier(
    val objectService: ObjectService,
    val logisticService: LogisticService,
    id: Id = v7(),
    val localMap: GameMap,
) : Actor(id), Carriable, TaskHandle<LogisticTask>, Staff {

    fun init() {
        queue.add(CheckTaskAction)
    }


    override var location: Location = Location.XY(0, 0)
    override var maxSpeed: Int = 20
    override var carrying: String? = null
    override var task: LogisticTask? = null
    override var speed: Speed = Speed.zero


    override val abilities: List<Ability> = listOf(
        MoveAbility(this, objectService),
        CarryAbility(this, objectService),
        LogisticAbility(this, logisticService, objectService, localMap)
    )


}
