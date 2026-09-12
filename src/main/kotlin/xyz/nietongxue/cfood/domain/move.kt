package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.right
import xyz.nietongxue.cfood.domain.Direction.*
import xyz.nietongxue.cfood.domain.action.*
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.PathFinder
import xyz.nietongxue.common.response.ResponseChainResult
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


class MoveAbility(val actor: Actor, val objectService: ObjectService) : Ability() {
    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        return if (actor is Movable)
            when (action) {
                is MoveAction -> {
                    if (actor.location.samePosition(action.dest)) {
                        actor.speed = Speed.zero
                        ActionResult(ActionOperation.Consume).also {
                            logging.info("到达目的地 - ${action.dest}")
                        }.right()
                    } else {
                        val index = action.plan.positions.indexOf(actor.location.position())
                        require(index >= 0)
                        val next = action.plan.positions[minOf(index + actor.maxSpeed, action.plan.positions.size - 1)]
                        val next2 =
                            action.plan.positions[minOf(index + (actor.maxSpeed * 2), action.plan.positions.size - 1)]
                        val speed =
                            Speed.fromTwoPositions(next, next2)
                        actor.speed = speed
                        actor.location = Location.XY(next.x, next.y)
                        if (actor is Carriable && actor.carrying != null)
                            objectService.setLocation(
                                actor.carrying!!,
                                actor.location,
                                actor.id
                            )
                        logging.info("移动中，已到 - ${actor.location}")
                        logging.info("移动中，速度 - ${actor.speed}")
                        logging.info("移动中，下个位置 - $next2")
                        ActionResult(
                            ActionOperation.ReplaceHead(action)
                        ).right()
                    }
                }

                else -> notMe
            } else notMe
    }
}

