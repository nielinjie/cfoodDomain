package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import java.time.Duration

data class Routing(
    override val id: Id = v7(),
    val productId: Id,
    val lines: List<RoutingLine>
) : HasId {
}

data class RoutingLine(
    override val id: Id = v7(),
    val operation: RoutingOperation,
    val quantity: Int
) : HasId

/**
 * 工艺步骤，相对于执行步骤
 */
data class RoutingOperation(
    override val id: Id = v7(),
    val code: String,
    val name: String,
    val time: Duration,
    val productId: Id, //生产什么东西？跟 bom 在此联系。
    val actionDescription: String,//动作
) : HasId


class RoutingService {
    val routingList = mutableListOf<Routing>()
    fun save(routing: Routing) {
        routingList.add(routing)
    }

    fun getByProductId(productId: Id): Routing? {
        return routingList.firstOrNull { it.productId == productId }
    }

}