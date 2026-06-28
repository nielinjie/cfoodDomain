package xyz.nietongxue.cfood.domain

import arrow.core.Either
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
    /**
     * 检查输入,
     * 如果满足，返回 objectId，
     * 否则返回 缺少的 productId
     */
    fun checkInput(existed: List<Object>): Either<String, List<Object>> {
        val existedPair = existed.map { it.productId to it.id }.toMutableSet()
        return checkInSet(existedPair).map { it.map { existed.first { e -> e.id == it } } }
    }

    private fun checkInSet(existedPair: MutableSet<Pair<Id, Id>>): Either<String, List<String>> {
        val used: MutableSet<Pair<String, String>> = mutableSetOf() // productId to objectId
        this.consume.group().forEach { (productId, quantity) ->
            for (i in 0 until quantity) {
                val found = existedPair.firstOrNull { it.first == productId }
                if (found == null)
                    return Either.Left(productId)
                else {
                    used.add(found)
                    existedPair.remove(found)
                }
            }
        }
        return Either.Right(used.map { it.second })
    }
}

fun List<Consume>.group(): Map<Id, Int> {
    return this.groupBy { it.product.id }.mapValues { it.value.sumOf { it.quantity } }
}

data class Consume(val product: Product, val quantity: Int) {
}

sealed interface OperationState {
    object WaitingForReady : OperationState
    object Ready : OperationState
    object Running : OperationState
    object Finished : OperationState
}

