package xyz.nietongxue.cfood.domain.patron

import xyz.nietongxue.cfood.domain.Direction
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.Speed
import xyz.nietongxue.cfood.domain.action.Ability
import xyz.nietongxue.cfood.domain.action.Actor
import xyz.nietongxue.common.base.Id

/**
 * 大口
 * action Seq:
 *  1. 找seat
 *  2. move to seat
 *  3. take seat
 *  4. order。
 *  5. wait for food
 *  6. eat
 */

class Mouth(
    id: Id
) : Patron, Actor(id) {
    init {

        queue.addAll(
            listOf(
                FindSeatAction(),
                OrderAction(),
                WaitToEatAction(),
                EatAction()
            )
        )
    }

    override var location: Location = Location.XY(0, 0)
    override var maxSpeed: Int = 1
    override var speed: Speed = Speed(Direction.S, 0)
    override val abilities: List<Ability>
        get() = TODO("Not yet implemented")
}

