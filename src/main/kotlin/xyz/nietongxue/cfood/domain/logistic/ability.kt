package xyz.nietongxue.cfood.domain.logistic

import arrow.core.Either
import arrow.core.right
import xyz.nietongxue.cfood.domain.CheckTaskAction
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.TaskStateUpdate
import xyz.nietongxue.cfood.domain.action.Ability
import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.action.ActionOperation.*
import xyz.nietongxue.cfood.domain.action.ActionResult
import xyz.nietongxue.cfood.domain.action.Actor
import xyz.nietongxue.cfood.domain.carry.LoadAction
import xyz.nietongxue.cfood.domain.carry.TransformAction
import xyz.nietongxue.cfood.domain.carry.UnloadAction
import xyz.nietongxue.cfood.domain.map.GameMap
import xyz.nietongxue.cfood.domain.move.Movable
import xyz.nietongxue.cfood.domain.move.MoveAction
import xyz.nietongxue.cfood.domain.move.PathPlan
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.response.ResponseChainResult.NotMe

class LogisticAbility(
    val actor: Actor,
    val logisticService: LogisticService,
    val objectService: ObjectService,
    val localMap: GameMap
) : Ability() {

    fun LogisticUnloadTask.toActions(actor: Movable): List<Action> {
        val obj: Id = objectService.getOneFree(this.productId)!!.id
        val objLocation: Location = objectService.getLocation(obj)
        return listOf(
            MoveAction(
                objLocation, PathPlan.create(
                    actor.location.position(), objLocation.position(), localMap
                ) // TODO 目前是静态寻路，在任务接受时已经确认了路线计划。
            ),
            LoadAction(objLocation, obj),
            MoveAction(
                this.dest, PathPlan.create(
                    objLocation.position(), this.dest.position(), localMap
                )
            ),
            UnloadAction(this.dest),
            TaskStateUpdate(this.id)
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
            ),
            LoadAction(objLocation, obj),
            MoveAction(
                this.destStation.location, PathPlan.create(
                    objLocation.position(), this.destStation.location.position(), localMap
                )
            ),
            TransformAction(
                this.destStation.location,
                obj,
                this.destStation
            ),
            TaskStateUpdate(this.id)
        )
    }


    override fun act(action: Action): Either<NotMe, ActionResult> {

        return if (actor is TaskHandle<*>) when (action) {
            is TaskStateUpdate -> {
                require(actor.task != null)
                logging.info("任务执行完成 - ${action.taskId}")
                logisticService.finish(actor.task!!)
                actor.task = null
                ActionResult(Consume)
                    .right()
            }

            is CheckTaskAction -> {
                (if (actor.task != null) ActionResult(
                    MoveToEnd(
                        action
                    )
                )
                else {
                    val task = logisticService.dispatch(actor.id)
                    require(actor is Movable)
                    if (task != null) {
                        (actor as TaskHandle<LogisticTask>).task = task
                        logging.info("接受任务 - $task")
                        ActionResult(
                            ListenLoop(
                                when (task) {
                                    is LogisticUnloadTask -> task.toActions(actor)
                                    is LogisticTransformTask -> task.toActions(actor)
                                    else -> error("unknown task type")
                                }, action
                            )
                        )
                    } else {
                        ActionResult(
                            MoveToEnd(
                                action
                            )
                        )
                    }
                }).right()
            }

            else -> notMe
        } else notMe
    }

}