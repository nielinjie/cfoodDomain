package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import java.time.LocalDateTime

class Order(
    override val id: Id = v7(),
    val code: String,
    val lines: List<OrderLine>,
    val requiredTime: LocalDateTime,
    var state: OrderState
) : HasId {
    fun satisfy(objectService: ObjectService, productService: ProductService, objects: List<Object>): Boolean {
        return lines.all { it.satisfy(objectService, productService = productService, objects) }
    }
}

class OrderLine(
    override val id: Id = v7(),
    val productCode: String,
    val quantity: Int
) : HasId {
    fun satisfy(objectService: ObjectService, productService: ProductService, objects: List<Object>): Boolean {
        val productId = productService.getByCode(productCode)?.id ?: return false
        return objects.count { it.productId == productId } == quantity
    }

}

interface OrderState {
    object Waiting : OrderState
    object Processing : OrderState
    object Finished : OrderState
}


interface OrderListener {
    fun orderChangeEvent(order: Order)
}

class OrderService(
    val productService: ProductService,
    val routingService: RoutingService,
    val bomService: BOMService,
) {
    val orders = mutableListOf<Order>()

    var listener: OrderListener? = null


    fun getWaiting(): List<Order> {
        return orders.filter { it.state == OrderState.Waiting }
    }

    fun accept(order: Order) {
        orders.add(order)
        orderEvent(order)
    }


    fun orderEvent(order: Order) {
        listener?.orderChangeEvent(order)
    }

    fun finish(order: Order) {
        order.state = OrderState.Finished
    }

}

data class OrderEvent(val order: Order)