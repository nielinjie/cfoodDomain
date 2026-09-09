package xyz.nietongxue.cfood.domain.station

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.facility.Port
import xyz.nietongxue.cfood.domain.facility.Size
import xyz.nietongxue.cfood.domain.operation.CheckInputAction
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.Name
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult


/**
 * 柜台
 * 完成交易的中心。
 * 收取订单，输出订单。
 */
class Counter(
    override val name: Name,
    id: Id = v7(),
    override val location: Location,
    override val size: Size,
    override val ports: List<Port>,
    val objectService: ObjectService,
    val orderService: OrderService,
    val productService: ProductService
) : Actor(id),Station {
    override val abilities: List<Ability> = listOf(
        object : Ability() {
            override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
                return when (action) {
                    is CheckInputAction -> {
                        val objects = objectService.getByOwner(id)
                        val grouped = objects.groupBy { it.labels.get("orderId") }
                        grouped.forEach { (orderId, objects) ->
                            val order = orderService.orders.find { it.id == orderId }!!
                            if (order.satisfy(objectService, productService, objects)) {
                                logging.info("订单完成 - $order")
                                orderService.finish(order)
                                logging.info("Object输出 - ${objects.map { it.id }}")
                                objectService.output(objects)
                            }
                        }
                        ActionResult(ActionOperation.MoveToEnd(action)).right()
                    }

                    else -> ResponseChainResult.NotMe.left()
                }
            }
        }
    )

    fun init() {
        queue.add(CheckInputAction())
    }
}