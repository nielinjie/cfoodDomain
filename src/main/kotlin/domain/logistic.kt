package xyz.nietongxue.cfood.domain

import arrow.core.Either
import arrow.core.right
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7
import xyz.nietongxue.common.response.ResponseChainResult


interface LogisticTask : Task

data class LogisticUnloadTask(
    override val id: Id = v7(), val productId: Id, val quantity: Int, val dest: Location
) : LogisticTask


data class LogisticTransformTask(
    override val id: Id = v7(), val productId: Id, val quantity: Int, val destStation: Station
) : LogisticTask

data class ProcessingLogistic(val carryId: Id, val objectIds: List<Id>) : TaskState


@Service
class LogisticService(
) : TaskManager {


    val logger = getLogger(this::class.java)

    val tasks = mutableListOf<LogisticTask>()
    val states = mutableMapOf<Id, TaskState>()
    fun logisticRequest(productId: String, quantity: Int, dest: Location) {
        val task = LogisticUnloadTask(productId = productId, quantity = quantity, dest = dest)
        tasks.add(task)
        states[task.id] = TaskState.Waiting
        logger.info("logistic task created -  $task")
    }

    fun logisticTransformRequest(productId: String, quantity: Int, dest: Station) {
        val task = LogisticTransformTask(productId = productId, quantity = quantity, destStation = dest)
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


data object CheckTaskAction : Action

interface LogisticTaskHandle : Movable {
    var task: LogisticTask?
}

class LogisticTaskWorkerCapability(
    val actor: Actor, val logisticService: LogisticService, val objectService: ObjectService, val localMap: GameMap
) : ActCapability() {
    val logger = getLogger(this::class.java)!!

    fun LogisticUnloadTask.toActions(actor: Movable): List<Action> {
        val obj: Id = objectService.getOneFree(this.productId)!!.id
        val objLocation: Location = objectService.getLocation(obj)
        return listOf(
            MoveAction(
                objLocation, PathPlan.create(
                    actor.location.position(), objLocation.position(), localMap
                ) // TODO 目前是静态寻路，在任务接受时已经确认了路线计划。
            ), LoadAction(objLocation, obj), MoveAction(
                this.dest, PathPlan.create(
                    objLocation.position(), this.dest.position(), localMap
                )
            ), UnloadAction(this.dest), TaskStateUpdate(this.id)
        )
    }

    fun LogisticTransformTask.toActions(actor: Movable): List<Action> {
        val obj: Id = objectService.getOneFree(this.productId)!!.id
        val objLocation: Location = objectService.getLocation(obj)
        return listOf(
            MoveAction(
                objLocation, PathPlan.create(
                    actor.location.position(), objLocation.position(), localMap
                )
            ), LoadAction(objLocation, obj), MoveAction(
                this.destStation.location, PathPlan.create(
                    objLocation.position(), this.destStation.location.position(), localMap
                )
            ), TransformAction(this.destStation.location, obj, this.destStation), TaskStateUpdate(this.id)
        )
    }


    override fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult> {

        return if (actor is LogisticTaskHandle) when (action) {
            is TaskStateUpdate -> {
                require(actor.task != null)
                logger.info("任务执行完成 - ${action.taskId}")
                logisticService.finish(actor.task!!)
                actor.task = null
                ActionResult(ActionEffect.Consume).right()
            }

            is CheckTaskAction -> {
                (if (actor.task != null) ActionResult(ActionEffect.MoveToEnd(action))
                else {
                    val task = logisticService.dispatch(actor.id)
                    if (task != null) {
                        actor.task = task
                        logger.info("接受任务 - $task")
                        ActionResult(
                            ActionEffect.ListenLoop(
                                when (task) {
                                    is LogisticUnloadTask -> task.toActions(actor)
                                    is LogisticTransformTask -> task.toActions(actor)
                                    else -> error("unknown task type")
                                }, action
                            )
                        )
                    } else {
                        ActionResult(ActionEffect.MoveToEnd(action))
                    }
                }).right()
            }

            else -> Either.Left(ResponseChainResult.NotMe)
        } else Either.Left(ResponseChainResult.NotMe)
    }

}