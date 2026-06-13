package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import kotlin.time.Duration

/**
 * 执行步骤，相对于工艺步骤。
 */

data class Operation(
    override val id: Id = v7(),
    val code: String,
    val name: String,
    val time: Duration,
    val product: Product,
    val consume: List<Consume>
) : HasId {
    fun ready(): ReadyState {
        return ReadyState.Ready
    }

    fun start() {}
    fun state(): OperationState {
        TODO()
    }
}
data class Consume(val product: Product, val quantity: Int) {
}

sealed interface OperationState {
    object WaitingForReady : OperationState
    object Ready : OperationState
    object Running : OperationState
    object Finished : OperationState
}

interface ReadyState {
    object Ready : ReadyState
    class NotReady(val lack: List<Lack>) : ReadyState
}

data class Lack(val productId: Id, val quantity: Int)