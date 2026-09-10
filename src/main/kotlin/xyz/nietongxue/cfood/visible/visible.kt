package xyz.nietongxue.cfood.visible

import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.facility.HouseService
import xyz.nietongxue.cfood.domain.staff.StaffService
import xyz.nietongxue.cfood.visible.Behavior.Walking.Companion.fromMovable
import xyz.nietongxue.common.collections.Change
import xyz.nietongxue.common.collections.changes


interface Model {
    object Person : Model
    object Table : Model
}

interface Behavior {
    object Idle : Behavior
    data class Walking(val direction: Direction) : Behavior {
        companion object {
            fun fromMovable(move: Movable): Behavior {
                return if (move.speed.distance == 0) Idle
                else Walking(move.speed.direction)
            }
        }
    }
}


data class Visible(
    val id: String,
    val position: Position,
    val speed: Speed,
    val behavior: Behavior,
    val model: Model = Model.Person
)

data class Rectangle(
    val position: Position,
    val size: Size
) {
    fun include(position: Position): Boolean {
        return position.x in position.x..position.x + size.width && position.y in position.y..position.y + size.height
    }
}


class VisualConfig(
    val window: Rectangle,
)

class SnapShot(val visibles: List<Visible>, val window: Rectangle, val version: Long) {
    fun getByIds(ids: List<String>): List<Visible> {
        return visibles.filter { it.id in ids }
    }

    fun diff(new: SnapShot): List<Change<Visible>> {
        return changes(this.visibles, new.visibles, { it.id }, { a, b -> a == b })
    }

}


@Service
class VisibleService(
    val objectService: ObjectService, val houseService: HouseService, val staffService: StaffService

) {

    var currentSnapShot: MutableMap<Long, SnapShot> = mutableMapOf()

    fun snapshot(window: Rectangle): SnapShot {
        return SnapShot((staffService.staff.map {
            Visible("v_s_${it.id}", it.location.position(), it.speed, fromMovable(it))
        } + houseService.facilities.map {
            Visible("v_f_${it.id}", it.location.position(), Speed.zero, Behavior.Idle, Model.Table)
        }).filter {
            window.include(it.position)
        }, window, System.currentTimeMillis())
    }

    fun changes(window: Rectangle, oldVersion: Long): List<Change<Visible>> {
        val thisVersion = System.currentTimeMillis()
        return snapshot(window).let {
            val re = currentSnapShot[oldVersion]!!.diff(it)
            currentSnapShot[thisVersion] = it
            re
        }
    }


}

