package xyz.nietongxue.cfood.domain.operation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.action.*
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.station.Station
import xyz.nietongxue.common.base.stuff
import xyz.nietongxue.common.response.ResponseChainResult


interface OperationHandler {
    val task: Operation
}

class OperateAbility(
    val actor: Actor,
    val operationTaskHandle: OperationTaskHandle,
    val hasLocation: HasLocation,
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val orchestrateService: OrchestrateService,

    val world: World
) : Ability() {

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
        return when (action) {
            //处理操作本身的能力。
            is RequestInputAction -> {
                require(operationTaskHandle.task != null)
                operationTaskHandle.task!!.operation.consume.group().forEach { (productId, quantity) ->
                    logisticService.logisticTransformRequest(productId, quantity, operationTaskHandle as Station).also {
                        logging.info("提交物流请求 - $it")
                    }
                }
                ActionResult(ActionOperation.Consume).right()
            }

            is CheckInputAction -> {
                if (operationTaskHandle.task == null) return ActionResult(ActionOperation.Consume).right()
                val existed = objectService.getByOwner(actor.id)
                when (val checkInput = operationTaskHandle.task!!.operation.checkInput(existed)) {
                    is Either.Left -> ActionResult(ActionOperation.MoveToEnd(action)).right().also {
                        logging.info("check input not satisfied - ${checkInput.left()}")
                    }

                    is Either.Right -> {
                        checkInput.value.forEach {
                            objectService.consume(it.id, actor.id)
                        }
                        ActionResult(
                            ActionOperation.ListenLoop(
                                operationTaskHandle.task!!.operation.toActions(checkInput.value),
                                action
                            )
                        ).right().also {
                            logging.info("check input done.")
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
                    logging.info("任务完成 - ${action.operation}")
                    val product = operation.product
                    objectService.product(product.id, 1, hasLocation.location, stuff("orderId" to operation.orderId))
                    logisticService.logisticTransformRequest(product.id, 1, world.getStation("counter")!!)
                    ActionResult(ActionOperation.Consume).right()
                } else {
                    logging.info("任务进行中 - ${action.operation}, progress = $progress")
                    ActionResult(ActionOperation.ReplaceHead(action.also {
                        it.progress = progress.toInt()
                    })).right()
                }
            }
            //处理关于任务执行的能力。
            is TaskStateUpdate -> {
                require(operationTaskHandle.task != null)
                logging.info("任务执行完成 - ${action.taskId}")
                orchestrateService.finish(operationTaskHandle.task!!)
                operationTaskHandle.task = null
                ActionResult(ActionOperation.Consume).right()
            }

            is CheckTaskAction -> {
                (if (operationTaskHandle.task != null) ActionResult(ActionOperation.MoveToEnd(action))
                else {
                    val task = orchestrateService.dispatch(actor.id)
                    if (task != null) {
                        operationTaskHandle.task = task
                        logging.info("接受任务 - $task")
                        ActionResult(ActionOperation.ListenLoop(task.toActions(), action))
                    } else ActionResult(ActionOperation.MoveToEnd(action))
                }).right()
            }

            else -> {
                notMe
            }
        }
    }
}