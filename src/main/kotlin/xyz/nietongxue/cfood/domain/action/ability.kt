package xyz.nietongxue.cfood.domain.action

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.nietongxue.common.response.ResponseChainResult
import xyz.nietongxue.common.base.Record


/**
 * 对特定 action 动作的实现。
 * 对 Action 的处理，体现了 Actor 的能力，所以叫 ability
 */
abstract class Ability {
    typealias DoAction = Either<ResponseChainResult.NotMe, ActionResult>

    abstract fun act(action: Action): DoAction
    val notMe = ResponseChainResult.NotMe.left()
    val logging = ActionLogging
    fun ActionOperation.done(): DoAction {
        return ActionResult(this).right()
    }
}

object ActionLogging : Logging() {
    override val logger = LoggerFactory.getLogger(this::class.java)!!

}

abstract class Logging {
    val records = mutableListOf<Record<String>>()
    abstract val logger: Logger
    fun info(msg: String) {
        records.add(Record(msg))
        logger.info(msg)
    }
}