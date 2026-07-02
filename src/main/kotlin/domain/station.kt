package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import xyz.nietongxue.common.base.HasName
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.Name
import xyz.nietongxue.common.base.stuff
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult
import kotlin.math.log
import kotlin.time.DurationUnit


class OperationTask(override val id: Id, val operation: Operation) : Task

interface Station : Actor, HasLocation, HasName {}

data class ProcessingOperation(val stationId: Id) : TaskState

class OperationAction(val operation: Operation, var progress: Int, val startedTime: Long) : Action
class CheckInputAction() : Action
class RequestInputAction() : Action


class Counter(
    override val name: Name,
    override val id: Id = v7(),
    override val location: Location,
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
                            if (order.satisfy(objectService, productService ,objects)) {
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

class Stove(
    override val name: String,
    override val id: Id = v7(),
    val orchestrateService: OrchestrateService,
    val objectService: ObjectService,
    val logisticService: LogisticService,
    val world: World
) : Station {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    override var location: Location = Location.XY(0, 0)
    var operationTask: OperationTask? = null

    fun init() {
        queue.add(CheckTaskAction)
    }


    fun OperationTask.toActions(): List<Action> {
        return listOf(
            RequestInputAction(),
            CheckInputAction(),
        )
    }

    fun Operation.toActions(inputs: List<Object>): List<Action> {
        return listOf(
            OperationAction(this, 0, System.currentTimeMillis()),
            TaskStateUpdate(this.id)
        )
    }


    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()
    override val actCapabilities: List<ActCapability> = listOf(object : ActCapability() {
        override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
            return when (action) {
                is RequestInputAction -> {
                    require(operationTask != null)
                    operationTask!!.operation.consume.group().forEach { (productId, quantity) ->
                        logisticService.logisticTransformRequest(productId, quantity, this@Stove).also {
                            logger.info("提交物流请求 - $it")
                        }
                    }
                    ActionResult(ActionEffect.Consume).right()
                }

                is CheckInputAction -> {
                    if (operationTask == null) return ActionResult(ActionEffect.Consume).right()
                    val existed = objectService.getByOwner(this@Stove.id)
                    val checkInput = operationTask!!.operation.checkInput(existed)
                    when (checkInput) {
                        is Either.Left -> ActionResult(ActionEffect.MoveToEnd(action)).right().also {
                            logger.info("check input not satisfied - ${checkInput.left()}")
                        }

                        is Either.Right -> {
                            checkInput.value.forEach {
                                objectService.consume(it.id, this@Stove.id)
                            }
                            ActionResult(
                                ActionEffect.ListenLoop(
                                    operationTask!!.operation.toActions(checkInput.value),
                                    action
                                )
                            ).right().also {
                                logger.info("check input done.")
                            }
                        }
                    }
                }

                is OperationAction -> {
                    val startedTime = action.startedTime
                    val operation = action.operation
                    val time = System.currentTimeMillis() - startedTime
                    val progress = (time.toDouble() / operation.time.toMillis()) * 100
                    if (progress >= 100) {
                        logger.info("任务完成 - ${action.operation}")
                        val product = operation.product
                        objectService.product(product.id, 1, this@Stove.location, stuff("orderId" to operation.orderId))
                        logisticService.logisticTransformRequest(product.id, 1, world.getStation("counter")!!)
                        ActionResult(ActionEffect.Consume).right()
                    } else {
                        logger.info("任务进行中 - ${action.operation}, progress = $progress")
                        ActionResult(ActionEffect.ReplaceHead(action.also {
                            it.progress = progress.toInt()
                        })).right()
                    }
                }

                else -> {
                    ResponseChainResult.NotMe.left()
                }
            }
        }
    }, object : ActCapability() {
        val actor = this@Stove
        override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
            return when (action) {
                is TaskStateUpdate -> {
                    require(actor.operationTask != null)
                    logger.info("任务执行完成 - ${action.taskId}")
                    orchestrateService.finish(actor.operationTask!!)
                    actor.operationTask = null
                    ActionResult(ActionEffect.Consume).right()
                }

                is CheckTaskAction -> {
                    (if (this@Stove.operationTask != null) ActionResult(ActionEffect.MoveToEnd(action))
                    else {
                        val task = orchestrateService.dispatch(this@Stove.id)
                        if (task != null) {
                            this@Stove.operationTask = task
                            logger.info("接受任务 - $task")
                            ActionResult(ActionEffect.ListenLoop(task.toActions(), action))
                        } else ActionResult(ActionEffect.MoveToEnd(action))
                    }).right()
                }

                else -> Either.Left(ResponseChainResult.NotMe)
            }
        }
    })

}

