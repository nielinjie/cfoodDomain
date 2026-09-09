package xyz.nietongxue.cfood.domain.patron

import xyz.nietongxue.cfood.domain.Ability
import xyz.nietongxue.cfood.domain.Actor
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.Speed
import xyz.nietongxue.common.base.Id

/**
 * 大口
 */

class Mouth(
     id: Id
) : Patron, Actor(id) {
    override var location: Location
        get() = TODO("Not yet implemented")
        set(value) {}
    override var speed: Speed
        get() = TODO("Not yet implemented")
        set(value) {}
    override val maxSpeed: Int
        get() = TODO("Not yet implemented")
    override val abilities: List<Ability>
        get() = TODO("Not yet implemented")
}

