package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.right
import org.slf4j.LoggerFactory
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.PathFinder
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.response.ResponseChainResult


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
    var speed: Int
}


class MoveCapability(val actor: Actor, val objectService: ObjectService) : ActCapability() {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        return if (actor is Movable)
            when (action) {
                is MoveAction -> {
                    if (actor.location.samePosition(action.dest)) ActionResult(ActionEffect.Consume).also {
                        logger.info("到达目的地 - ${action.dest}")
                    }.right()
                    else {
                        val index = action.plan.positions.indexOf(actor.location.position())
                        require(index >= 0)
                        val next = action.plan.positions[minOf(index + actor.speed, action.plan.positions.size - 1)]
                        actor.location = Location.XY(next.x, next.y)
                        if (actor is Carriable && actor.carrying != null)
                            objectService.setLocation(
                                actor.carrying!!,
                                actor.location,
                                actor.id
                            )
                        logger.info("移动中，已到 - ${actor.location}")
                        ActionResult(
                            ActionEffect.ReplaceHead(action)
                        ).right()
                    }
                }

                else -> Either.Left(ResponseChainResult.NotMe)
            } else Either.Left(ResponseChainResult.NotMe)
    }
}

