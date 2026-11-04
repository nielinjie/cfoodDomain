package xyz.nietongxue.cfood.domain.patron

import arrow.core.Either
import arrow.core.right
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.World
import xyz.nietongxue.cfood.domain.action.Ability
import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.action.ActionOperation
import xyz.nietongxue.cfood.domain.action.ActionResult
import xyz.nietongxue.cfood.domain.facility.HouseService
import xyz.nietongxue.cfood.domain.move.MoveAction
import xyz.nietongxue.cfood.domain.move.PathPlan
import xyz.nietongxue.common.response.ResponseChainResult

class TakeSeatAbility(val patron: Patron, val houseService: HouseService, val world: World) : Ability() {
    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        return when (action) {
            is FindSeatAction -> {
                val seat = houseService.findSeat(patron)
                if (seat == null)
                    ActionResult(ActionOperation.ReplaceHead(action)).right()
                else {
                    val seatLocation = seat.position()
                    val move = MoveAction(
                        Location.XY(seatLocation),
                        PathPlan.create(patron.location.position(), seatLocation, world.localMap!!)
                    )
                    val seatDown = TryTakeSeatAction(seat)
                    ActionResult(ActionOperation.ReplaceHead(move, seatDown)).right()
                }
            }

            is TryTakeSeatAction -> {
                //检查位置。
                if (action.port.position() == patron.location.position()) {
                    if (houseService.seatAvailable(action.port)) {
                        houseService.takeSeat(action.port, patron.id)
                        ActionResult(ActionOperation.Consume).right()
                    } else {
                        ActionOperation.ReplaceHead(action).done()//等待。或许应该再找位子？
                    }
                } else {
                    ActionResult(
                        ActionOperation.ReplaceHead(
                            MoveAction(
                                Location.XY(action.port.position()),
                                PathPlan.create(patron.location.position(), action.port.position(), world.localMap!!)
                            ), action
                        )
                    ).right()
                }
            }

            else -> {
                notMe
            }
        }
    }
}
