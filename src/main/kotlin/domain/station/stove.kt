package xyz.nietongxue.cfood.domain.station

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import xyz.nietongxue.cfood.domain.ActCapability
import xyz.nietongxue.cfood.domain.Action
import xyz.nietongxue.cfood.domain.ActionEffect
import xyz.nietongxue.cfood.domain.ActionResult
import xyz.nietongxue.cfood.domain.CheckTaskAction
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.LogisticService
import xyz.nietongxue.cfood.domain.Object
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.Operation
import xyz.nietongxue.cfood.domain.OrchestrateService
import xyz.nietongxue.cfood.domain.Port
import xyz.nietongxue.cfood.domain.Size
import xyz.nietongxue.cfood.domain.TaskStateUpdate
import xyz.nietongxue.cfood.domain.World
import xyz.nietongxue.cfood.domain.group
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.stuff
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 灶台
 * 一种加工工作站。负责一种加工。
 */
class Stove(
    override val name: String,
    override val id: Id = v7(),
    override val size: Size,
    override val ports: List<Port>,
    override var location: Location,
    val orchestrateService: OrchestrateService,
    val objectService: ObjectService,
    val logisticService: LogisticService,
    val world: World
) : Station {
    val logger = LoggerFactory.getLogger(this::class.java)!!
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

