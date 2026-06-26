package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.right
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult


data class LogisticTask(
    override val id: Id = v7(),
    val productId: Id,
    val quantity: Int,
    val dest: Location
) : HasId, Task

data class ProcessingLogistic(val carryId: Id, val objectIds: List<Id>) : TaskState

interface TaskState {
    object Waiting : TaskState
    object Finished : TaskState
}

@Service
class LogisticService(
) : TaskManager {


    val logger = getLogger(this::class.java)

    val tasks = mutableListOf<LogisticTask>()
    val states = mutableMapOf<Id, TaskState>()
    fun logisticRequest(productId: String, quantity: Int, dest: Location) {
        val task = LogisticTask(productId = productId, quantity = quantity, dest = dest)
        tasks.add(task)
        states[task.id] = TaskState.Waiting
        logger.info("logistic task created -  $task")
    }

    override fun dispatch(acceptId: Id): LogisticTask? {
        return tasks.firstOrNull { task ->
            states[task.id] == TaskState.Waiting
        }?.also { task ->
            states[task.id] = ProcessingLogistic(acceptId, listOf())
        }?.also {
            logger.info("dispatch task - $it")
        }
    }

    override fun finish(task: Task) {
        require(task is LogisticTask)
        states[task.id] = TaskState.Finished
        logger.info("finish task - $task")
    }
}

data class TaskStateUpdate(val taskId: String) : Action

interface LogisticTaskHandle {
    var task: LogisticTask?
}

class LogisticTaskWorkerCapability(
    val actor: Actor,
    val logisticService: LogisticService,
) : ActCapability() {
    val logger = getLogger(this::class.java)!!


    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {

        return if (actor is LogisticTaskHandle) when (action) {
            is TaskStateUpdate -> {
                require(actor.task != null)
                logger.info("任务执行完成 - ${action.taskId}")
                logisticService.finish(actor.task!!)
                actor.task = null
                ActionResult(ActionEffect.Consume).right()
            }

            else -> Either.Left(ResponseChainResult.NotMe)
        } else Either.Left(ResponseChainResult.NotMe)
    }

}