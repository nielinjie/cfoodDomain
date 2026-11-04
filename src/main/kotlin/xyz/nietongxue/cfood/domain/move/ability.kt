package xyz.nietongxue.cfood.domain.move

import arrow.core.Either
import arrow.core.right
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.action.*
import xyz.nietongxue.cfood.domain.carry.Carriable
import xyz.nietongxue.common.response.ResponseChainResult


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

