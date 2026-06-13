package xyz.nietongxue.cfood.domain

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import java.time.LocalDateTime

class Order(
    override val id: Id = v7(),
    val code: String,
    val lines: List<OrderLine>,
    val requiredTime: LocalDateTime,
    val status: OrderState
) : HasId

class OrderLine(
    override val id: Id = v7(),
    val productId: Id,
    val quantity: Int
) : HasId

interface OrderState {
    object Waiting : OrderState
    object Processing : OrderState
    object Finished : OrderState
}

@Service
class OrderService(
    val productService: ProductService,
    val routingService: RoutingService,
    val bomService: BOMService,
    val applicationContext: ApplicationContext
) {
    val orders = mutableListOf<Order>()


    fun getWaiting(): List<Order> {
        return orders.filter { it.status == OrderState.Waiting }
    }

    fun accept(order: Order) {
        orders.add(order)
        orderEvent(order)
    }


    fun orderEvent(order: Order) {
        applicationContext.publishEvent(OrderEvent(order))
    }


}

data class OrderEvent(val order: Order)