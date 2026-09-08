package xyz.nietongxue.cfood.domain.operation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.station.Station
import xyz.nietongxue.cfood.domain.station.Stove
import xyz.nietongxue.common.base.stuff
import xyz.nietongxue.common.response.ResponseChainResult


interface OperationHandler {
    val task: Operation
}

class OperationHandleCapability(
    val actor: Station,
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val orchestrateService: OrchestrateService,

    val world: World
) : ActCapability() {
    val logger = LoggerFactory.getLogger(this::class.java)

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

    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        val thisActor = actor as Stove
        return when (action) {
            is RequestInputAction -> {
                require(thisActor.operationTask != null)
                thisActor.operationTask!!.operation.consume.group().forEach { (productId, quantity) ->
                    logisticService.logisticTransformRequest(productId, quantity, thisActor).also {
                        logger.info("提交物流请求 - $it")
                    }
                }
                ActionResult(ActionEffect.Consume).right()
            }

            is CheckInputAction -> {
                if (thisActor.operationTask == null) return ActionResult(ActionEffect.Consume).right()
                val existed = objectService.getByOwner(thisActor.id)
                val checkInput = thisActor.operationTask!!.operation.checkInput(existed)
                when (checkInput) {
                    is Either.Left -> ActionResult(ActionEffect.MoveToEnd(action)).right().also {
                        logger.info("check input not satisfied - ${checkInput.left()}")
                    }

                    is Either.Right -> {
                        checkInput.value.forEach {
                            objectService.consume(it.id, thisActor.id)
                        }
                        ActionResult(
                            ActionEffect.ListenLoop(
                                thisActor.operationTask!!.operation.toActions(checkInput.value),
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
                    objectService.product(product.id, 1, thisActor.location, stuff("orderId" to operation.orderId))
                    logisticService.logisticTransformRequest(product.id, 1, world.getStation("counter")!!)
                    ActionResult(ActionEffect.Consume).right()
                } else {
                    logger.info("任务进行中 - ${action.operation}, progress = $progress")
                    ActionResult(ActionEffect.ReplaceHead(action.also {
                        it.progress = progress.toInt()
                    })).right()
                }
            }

            is TaskStateUpdate -> {
                require(actor.operationTask != null)
                logger.info("任务执行完成 - ${action.taskId}")
                orchestrateService.finish(actor.operationTask!!)
                actor.operationTask = null
                ActionResult(ActionEffect.Consume).right()
            }

            is CheckTaskAction -> {
                (if (thisActor.operationTask != null) ActionResult(ActionEffect.MoveToEnd(action))
                else {
                    val task = orchestrateService.dispatch(thisActor.id)
                    if (task != null) {
                        thisActor.operationTask = task
                        logger.info("接受任务 - $task")
                        ActionResult(ActionEffect.ListenLoop(task.toActions(), action))
                    } else ActionResult(ActionEffect.MoveToEnd(action))
                }).right()
            }

            else -> {
                ResponseChainResult.NotMe.left()
            }
        }
    }
}