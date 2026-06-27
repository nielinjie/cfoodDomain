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

data class LoadAction(val location: Location, val obj: Id) : Action
data class UnloadAction(val location: Location) : Action

data class TransformAction(val location: Location, val obj: Id, val accepter: Station) : Action
interface Carriable {
    var carrying: String?
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

class CarryCapability(val actor: Actor, val objectService: ObjectService) : ActCapability() {
    val logger = LoggerFactory.getLogger(this::class.java)!!

    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        return if (actor is Movable && actor is Carriable)
            when (action) {
                is LoadAction -> {
                    require(action.location.samePosition(actor.location))
                    require(actor.carrying == null)
                    require(objectService.getLocation(action.obj).samePosition(actor.location))
                    actor.carrying = action.obj
                    objectService.lock(action.obj, actor.id)
                    ActionResult(ActionEffect.Consume).also {
                        logger.info("载入对象 - ${action.obj}")
                    }.right()
                }

                is UnloadAction -> {
                    require(action.location.samePosition(actor.location))
                    require(actor.carrying != null)
                    val obj = actor.carrying
                    actor.carrying = null
                    objectService.release(obj!!, actor.id)
                    ActionResult(ActionEffect.Consume).also {
                        logger.info("卸载对象 - $obj")
                    }.right()
                }

                is TransformAction -> {
                    require(action.location.samePosition(actor.location))
                    require(action.location.samePosition(action.accepter.location))
                    require(actor.carrying != null)
                    val obj = actor.carrying
                    actor.carrying = null
                    objectService.transfer(obj!!, action.accepter.id, actor.id)
                    ActionResult(ActionEffect.Consume).also {
                        logger.info("转交对象 - $obj to ${action.accepter.id}")
                    }.right()
                }

                else -> Either.Left(ResponseChainResult.NotMe)
            }
        else Either.Left(ResponseChainResult.NotMe)
    }
}