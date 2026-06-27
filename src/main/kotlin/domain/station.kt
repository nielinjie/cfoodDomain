package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult
import kotlin.time.DurationUnit


class OperationTask(override val id: Id, val operation: Operation) : Task

interface Station : Actor, HasLocation {}

data class ProcessingOperation(val stationId: Id) : TaskState

class OperationAction(val operation: Operation, var progress: Int, val startedTime: Long) : Action


@Component
@Scope(SCOPE_PROTOTYPE)
class Stove(
//    val name: String, TODO name 还是要有的。
    @param:Autowired(required = false) override val id: Id = v7(),
    val orchestrateService: OrchestrateService
) : Station {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    override var location: Location = Location.XY(0, 0)
    var operationTask: OperationTask? = null

    @PostConstruct
    fun init() {
        queue.add(CheckTaskAction)
    }


    fun OperationTask.toActions(station: Station): List<Action> {
        return listOf(
            OperationAction(operation, 0, System.currentTimeMillis()), TaskStateUpdate(this.id)
        )
    }


    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()
    override val actCapabilities: List<ActCapability> = listOf(object : ActCapability() {
        override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {
            return when (action) {
                is OperationAction -> {
                    val startedTime = action.startedTime
                    val operation = action.operation
                    val time = System.currentTimeMillis() - startedTime
                    val progress = (time.toDouble() / operation.time.toLong(DurationUnit.MILLISECONDS)) * 100
                    if (progress >= 100) {
                        logger.info("任务完成 - ${action.operation}")
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

            return if (actor is Station) when (action) {
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
                            ActionResult(ActionEffect.ListenLoop(task.toActions(this@Stove), action))
                        } else ActionResult(ActionEffect.MoveToEnd(action))
                    }).right()
                }

                else -> Either.Left(ResponseChainResult.NotMe)
            } else Either.Left(ResponseChainResult.NotMe)
        }
    })

}

