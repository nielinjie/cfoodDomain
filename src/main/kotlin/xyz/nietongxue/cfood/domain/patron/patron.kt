package xyz.nietongxue.cfood.domain.patron

import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.facility.Port
import xyz.nietongxue.cfood.domain.move.Movable
import xyz.nietongxue.common.base.HasId


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


class OrderAction:Action
class WaitToEatAction:Action
class EatAction:Action

class OrderAndWaitAbility()