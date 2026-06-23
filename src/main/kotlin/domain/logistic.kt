package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.right
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult


data class LogisticTask(
    override val id: Id = v7(),
    val productId: Id,
    val quantity: Int,
    val dest: Location
) : HasId

interface LogisticTaskState {
    object Waiting : LogisticTaskState
    data class Processing(val carryId: Id, val objectIds: List<Id>) : LogisticTaskState
    object Finished : LogisticTaskState
}

@Service
class LogisticService {

    val logger = getLogger(this::class.java)

    val logisticTasks = mutableListOf<LogisticTask>()
    val states = mutableMapOf<Id, LogisticTaskState>()
    fun logisticRequest(productId: String, quantity: Int, dest: Location) {
        val task = LogisticTask(productId = productId, quantity = quantity, dest = dest)
        logisticTasks.add(task)
        states[task.id] = LogisticTaskState.Waiting
        logger.info("logistic task created -  $task")
    }

    fun dispatch(carryId: Id): LogisticTask? {
        return logisticTasks.firstOrNull { task ->
            states[task.id] == LogisticTaskState.Waiting
        }?.also { task ->
            states[task.id] = LogisticTaskState.Processing(carryId, listOf())
        }?.also {
            logger.info("dispatch task - $it")
        }
    }

    fun finish(task: LogisticTask) {
        states[task.id] = LogisticTaskState.Finished
        logger.info("finish task - $task")
    }
}

data class TaskStateUpdate(val taskId: String) : Action
data class CheckTaskAction(val taskType: String = "Logistic") : Action

interface LogisticTaskHandle {
    var task: LogisticTask?
}

class LogisticTaskWorkerCapability(
    val actor: Actor,
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val localMap: GameMap
) : ActCapability() {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    fun LogisticTask.toActions(actor: Movable): List<Action> {
        val obj: Id = objectService.getOneFree(this.productId)!!.id
        val objLocation: Location = objectService.getLocation(obj)
        return listOf(
            MoveAction(
                objLocation,
                PathPlan.create(
                    actor.location.position(),
                    objLocation.position(),
                    localMap
                ) // TODO 目前是静态寻路，在任务接受时已经确认了路线计划。
            ),
            LoadAction(objLocation, obj),
            MoveAction(
                this.dest,
                PathPlan.create(
                    objLocation.position(),
                    this.dest.position(),
                    localMap
                )// TODO 目前是静态寻路，在任务接受时已经确认了路线计划。
            ),
            UnloadAction(this.dest),
            TaskStateUpdate(this.id)
        )
    }

    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
        fun acceptTask(task: LogisticTask) {
            require(actor is LogisticTaskHandle && actor is Movable)
            require(actor.task == null)
            actor.task = task
            logger.info("接受任务 - $task")
            actor.accept(*task.toActions(actor).toTypedArray())
        }
        return if (actor is LogisticTaskHandle) when (action) {
            is TaskStateUpdate -> {
                require(actor.task != null)
                logger.info("任务执行完成 - ${action.taskId}")
                logisticService.finish(actor.task!!)
                actor.task = null
                actor.accept(CheckTaskAction())
                ActionResult(action, true).right()
            }

            is CheckTaskAction -> {
                require(actor.task == null)
                require(action.taskType == "Logistic")
                val task = logisticService.dispatch(actor.id)

                if (task != null) {
                    logger.info("发现任务 - $task")
                    acceptTask(task)
                    ActionResult(action, true).right()
                } else {
                    logger.info("无任务")
                    ActionResult(action, false).right()
                }
            }


            else -> Either.Left(ResponseChainResult.NotMe)
        } else Either.Left(ResponseChainResult.NotMe)
    }

}