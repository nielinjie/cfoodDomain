package xyz.nietongxue.cfood.domain.move

import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.Position
import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.map.GameMap
import xyz.nietongxue.cfood.domain.move.Direction.*
import xyz.nietongxue.cfood.domain.path.PathFinder
import kotlin.math.sqrt

enum class Direction {
    N, S, E, W,
    NE, NW, SE, SW, NONE;


    companion object {
        fun fromString(direction: String): Direction {
            return when (direction.uppercase()) {
                "N", "NORTH" -> N
                "S", "SOUTH" -> S
                "E", "EAST" -> E
                "W", "WEST" -> W
                "NE", "NORTHEAST" -> NE
                "NW", "NORTHWEST" -> NW
                "SE", "SOUTHEAST" -> SE
                "SW", "SOUTHWEST" -> SW
                "NONE", "NONE" -> NONE
                else -> error("Invalid direction: $direction")
            }
        }

        fun fromTwoPositions(start: Position, end: Position): Direction {
            val dx = end.x - start.x
            val dy = end.y - start.y

            return when {
                dx > 0 && dy < 0 -> NE
                dx < 0 && dy < 0 -> NW
                dx > 0 && dy > 0 -> SE
                dx < 0 && dy > 0 -> SW
                dx > 0 -> E
                dx < 0 -> W
                dy < 0 -> N
                dy > 0 -> S
                else -> NONE
            }
        }
    }
}



/**
 * dy/dx
 */
data class Degree(val dy: Double, val dx: Double) {

    fun getDirection(): Direction {
        return when {
            dx > 0 && dy < 0 -> NE
            dx < 0 && dy < 0 -> NW
            dx > 0 && dy > 0 -> SE
            dx < 0 && dy > 0 -> SW
            dx > 0 -> E
            dx < 0 -> W
            dy < 0 -> N
            dy > 0 -> S
            else -> NONE
        }
    }
}


data class Speed(val degree: Degree, val direction: Direction = degree.getDirection(), val distance: Double) {
    companion object {
        val zero = Speed(Degree(0.0, 0.0), distance = 0.0)
        fun fromTwoPositions(start: Position, end: Position): Speed {
            val dx = end.x - start.x
            val dy = end.y - start.y

            if (dx == 0 && dy == 0) return zero

            val distance = sqrt((dx * dx + dy * dy).toDouble())
            return Speed(Degree(dy.toDouble(), dx.toDouble()), distance = distance)
        }
    }
}

class PathPlan(val positions: List<Position>) {
    companion object {
        fun create(start: Position, dest: Position, map: GameMap): PathPlan {
            val path = PathFinder(map).findPath(start, dest)
            return PathPlan(path)
        }
    }
}

class MoveAction(val dest: Location, var plan: PathPlan) : Action
interface HasLocation {
    val location: Location
}

interface Movable : HasLocation {
    override var location: Location
    var speed: Speed
    val maxSpeed: Int
}

