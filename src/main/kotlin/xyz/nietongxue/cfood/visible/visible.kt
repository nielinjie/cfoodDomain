package xyz.nietongxue.cfood.visible

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.UniverseConfig
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.facility.HouseService
import xyz.nietongxue.cfood.domain.facility.Table
import xyz.nietongxue.cfood.domain.staff.StaffService
import xyz.nietongxue.cfood.domain.station.Counter
import xyz.nietongxue.cfood.domain.station.Stove
import xyz.nietongxue.cfood.visible.Pose.Walking.Companion.fromMovable
import xyz.nietongxue.common.collections.Change
import xyz.nietongxue.common.collections.changes
import xyz.nietongxue.common.json.JsonWithType

@JsonWithType
interface Model {
    object Person : Model
    object Table : Model
    object Stove : Model
    object Counter : Model
}

@JsonWithType
interface Pose {
    object Idle : Pose
    data class Walking(val direction: Direction) : Pose {
        companion object {
            fun fromMovable(move: Movable): Pose {
                return if (move.speed.distance == 0.0) Idle
                else Walking(move.speed.direction)
            }
        }
    }
}


data class Visible(
    val id: String, val position: Position, val speed: Speed, val pose: Pose, val model: Model = Model.Person
)

data class Rectangle(
    val position: Position, val size: Size
) {
    fun include(position: Position): Boolean {
        return position.x in position.x..position.x + size.width && position.y in position.y..position.y + size.height
    }
}


class VisualConfig(
    val window: Rectangle,
)

data class SnapShot(
    val visibles: List<Visible>,
    val window: Rectangle,
    val version: Long,
    val timeStep: Int,
    val broadcastStep: Int
) {

    @JsonIgnore
    fun getByIds(ids: List<String>): List<Visible> {
        return visibles.filter { it.id in ids }
    }

    fun diff(new: SnapShot): List<Change<Visible>> {
        return changes(this.visibles, new.visibles, { it.id }, { a, b -> a == b })
    }

}


@Service
class VisibleService(
    val objectService: ObjectService, val houseService: HouseService, val staffService: StaffService,
    val universeConfig: UniverseConfig
) {

    var currentSnapShot: MutableMap<Long, SnapShot> = mutableMapOf()

    fun snapshot(window: Rectangle): SnapShot {
        return SnapShot(
            (staffService.staff.map {
                Visible("v_s_${it.id}", it.location.position(), it.speed, fromMovable(it), Model.Person)
            } + houseService.facilities.map {
                Visible(
                    "v_f_${it.id}", it.location.position(), Speed.zero, Pose.Idle, when (it) {
                        is Table -> Model.Table
                        is Stove -> Model.Stove
                        is Counter -> Model.Counter
                        else -> throw RuntimeException("Unknown facility type")
                    }
                )
            }).filter {
                window.include(it.position)
            },
            window, System.currentTimeMillis(), universeConfig.timeStep, universeConfig.broadcastStep
        )
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

