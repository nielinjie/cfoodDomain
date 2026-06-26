package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult
import kotlin.time.DurationUnit


class OperationTask(override val id: Id, val operation: Operation) : Task

interface Station : Actor, TaskWatcher, HasLocation {
}

data class ProcessingOperation(val stationId: Id) : TaskState

class OperationAction(val operation: Operation, var progress: Int, val startedTime: Long) : Action

@Component
@Scope(SCOPE_PROTOTYPE)
class Stove(
    val name: String,
    @param:Autowired(required = false)
    override val id: Id = v7(),
    val orchestrateService: OrchestrateService
) : Station {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    override var location: Location = Location.XY(0, 0)
    var operationTask: OperationTask? = null


    fun OperationTask.toActions(station: Station): List<Action> {
        return listOf(
            OperationAction(operation, 0, System.currentTimeMillis()),
            TaskStateUpdate(this.id)
        )
    }

    override val taskWatchingList: List<TaskWatching> = listOf(
        object : TaskWatching {
            override fun pull() {
                if (this@Stove.operationTask != null) return
                val task = orchestrateService.dispatch(this@Stove.id)
                if (task != null) {
                    this@Stove.operationTask = task
                    logger.info("接受任务 - $task")
                    accept(*task.toActions(this@Stove).toTypedArray<Action>())
                }
            }
        }
    )


    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()
    override val actCapabilities: List<ActCapability> = listOf(
        object : ActCapability() {
            override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
                return when (action) {
                    is OperationAction -> {
                        val startedTime = action.startedTime
                        val operation = action.operation
                        val time = System.currentTimeMillis() - startedTime
                        val progress = time / operation.time.toLong(DurationUnit.MILLISECONDS)
                        if (progress >= 100) {
                            ActionResult(action, true).right()
                        } else {
                            ActionResult(action, false).right()
                        }
                    }

                    else -> {
                        ResponseChainResult.NotMe.left()
                    }
                }
            }
        },
        object : ActCapability() {
            val actor = this@Stove
            override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {

                return if (actor is Station) when (action) {
                    is TaskStateUpdate -> {
                        require(actor.operationTask != null)
                        logger.info("任务执行完成 - ${action.taskId}")
                        orchestrateService.finish(actor.operationTask!!)
                        actor.operationTask = null
                        ActionResult(action, true).right()
                    }

                    else -> Either.Left(ResponseChainResult.NotMe)
                } else Either.Left(ResponseChainResult.NotMe)
            }
        }
    )

}

