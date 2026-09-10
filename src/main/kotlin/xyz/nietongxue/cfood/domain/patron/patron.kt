package xyz.nietongxue.cfood.domain.patron

import arrow.core.Either
import arrow.core.right
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.action.Ability
import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.action.ActionOperation
import xyz.nietongxue.cfood.domain.action.ActionOperation.ReplaceHead
import xyz.nietongxue.cfood.domain.action.ActionResult
import xyz.nietongxue.cfood.domain.facility.HouseService
import xyz.nietongxue.cfood.domain.facility.Port
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.response.ResponseChainResult


/**
 * 食客
 * action Seq:
 *  1. 找seat
 *  2. move to seat
 *  3. take seat
 *  4. order。
 *  5. wait for food
 *  6. eat
 */
interface Patron : HasId, Movable, Fighting, Satisfy {
}

interface Fighting

interface Satisfy {
}

class FindSeatAction : Action
class TryTakeSeatAction(val port: Port) : Action

class TakeSeatAbility(val patron: Patron, val houseService: HouseService, val world: World) : Ability() {
    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        return when (action) {
            is FindSeatAction -> {
                val seat = houseService.findSeat(patron)
                if (seat == null)
                    ActionResult(ReplaceHead(action)).right()
                else {
                    val seatLocation = seat.position()
                    val move = MoveAction(
                        Location.XY(seatLocation),
                        PathPlan.create(patron.location.position(), seatLocation, world.localMap!!)
                    )
                    val seatDown = TryTakeSeatAction(seat)
                    ActionResult(ReplaceHead(move, seatDown)).right()
                }
            }

            is TryTakeSeatAction -> {
                //检查位置。
                if (action.port.position() == patron.location.position()) {
                    if (houseService.seatAvailable(action.port)) {
                        houseService.takeSeat(action.port, patron.id)
                        ActionResult(ActionOperation.Consume).right()
                    } else {
                        ReplaceHead(action).done()//等待。或许应该再找位子？
                    }
                } else {
                    ActionResult(
                        ReplaceHead(
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

class OrderAction:Action
class WaitToEatAction:Action
class EatAction:Action

class OrderAndWaitAbility()