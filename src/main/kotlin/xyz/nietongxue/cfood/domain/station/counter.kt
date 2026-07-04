package xyz.nietongxue.cfood.domain.station

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import xyz.nietongxue.cfood.domain.ActCapability
import xyz.nietongxue.cfood.domain.Action
import xyz.nietongxue.cfood.domain.ActionEffect
import xyz.nietongxue.cfood.domain.ActionResult
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.OrderService
import xyz.nietongxue.cfood.domain.facility.Port
import xyz.nietongxue.cfood.domain.ProductService
import xyz.nietongxue.cfood.domain.facility.Size
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.Name
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult
import kotlin.collections.component1
import kotlin.collections.component2


/**
 * 柜台
 * 完成交易的中心。
 * 收取订单，输出订单。
 */
class Counter(
    override val name: Name,
    override val id: Id = v7(),
    override val location: Location,
    override val size: Size,
    override val ports: List<Port>,
    val objectService: ObjectService,
    val orderService: OrderService,
    val productService: ProductService
) : Station {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()
    override val actCapabilities: List<ActCapability> = listOf(
        object : ActCapability() {
            override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
                return when (action) {
                    is CheckInputAction -> {
                        val objects = objectService.getByOwner(id)
                        val grouped = objects.groupBy { it.labels.get("orderId") }
                        grouped.forEach { (orderId, objects) ->
                            val order = orderService.orders.find { it.id == orderId }!!
                            if (order.satisfy(objectService, productService, objects)) {
                                logger.info("订单完成 - $order")
                                orderService.finish(order)
                                logger.info("Object输出 - ${objects.map { it.id }}")
                                objectService.output(objects)
                            }
                        }
                        ActionResult(ActionEffect.MoveToEnd(action)).right()
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