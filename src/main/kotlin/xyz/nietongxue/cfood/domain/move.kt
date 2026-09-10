package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.right
import xyz.nietongxue.cfood.domain.action.*
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.PathFinder
import xyz.nietongxue.common.response.ResponseChainResult

enum class Direction {
    N, S, E, W,
    NE, NW, SE, SW;

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
                else -> error("Invalid direction: $direction")
            }
        }

        fun fromTwoPositions(start: Position, end: Position): Direction {
            val dx = end.x - start.x
            val dy = end.y - start.y
            return when {
                dx == 0 && dy == 0 -> S //default
                dx > 0 && dy == 0 -> E
                dx < 0 && dy == 0 -> W
                dx == 0 && dy > 0 -> N
                dx == 0 && dy < 0 -> S
                else -> error("Invalid direction") //TODO 支持斜向的。
            }
        }
    }
}


data class Speed(val direction: Direction, val distance: Int){
    companion object{
        val zero = Speed(Direction.S, 0)
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
                    if (actor.location.samePosition(action.dest)) ActionResult(ActionOperation.Consume).also {
                        logging.info("到达目的地 - ${action.dest}")
                    }.right()
                    else {
                        val index = action.plan.positions.indexOf(actor.location.position())
                        require(index >= 0)
                        val next = action.plan.positions[minOf(index + actor.maxSpeed, action.plan.positions.size - 1)]
                        val speed =
                            Speed(Direction.fromTwoPositions(actor.location.position(), next), actor.maxSpeed)
                        actor.speed = speed
                        actor.location = Location.XY(next.x, next.y)
                        if (actor is Carriable && actor.carrying != null)
                            objectService.setLocation(
                                actor.carrying!!,
                                actor.location,
                                actor.id
                            )
                        logging.info("移动中，已到 - ${actor.location}")
                        ActionResult(
                            ActionOperation.ReplaceHead(action)
                        ).right()
                    }
                }

                else -> notMe
            } else notMe
    }
}

