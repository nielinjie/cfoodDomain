package xyz.nietongxue.cfood.domain

import org.slf4j.LoggerFactory.getLogger
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id


class CarrierOld(override val id: Id) : HasId {
    var currentTask: LogisticTask? = null
    var location: Location? = null
    var carrying: Object? = null
    fun accept(task: LogisticTask) {
        currentTask = task
    }


}

class LoadAction(val location: Location, val obj: Id) : Action
class UnloadAction(val location: Location) : Action
class TaskStateUpdate(val taskId: String) : Action


class Carrier() : Actor() {
    val logger = getLogger(this::class.java)!!
    var location: Location = Location.XY(0, 0)
    val speed: Int = 1
    var carrying: String? = null
    override fun doIt(action: Action): ActionResult {
        return when (action) {
            is MoveAction -> {
                if (this.location == action.dest) ActionResult(action, true).also {
                    logger.info("到达目的地 - ${action.dest}")
                }
                else {
                    val stepVector = (action.dest.minus(this.location)).step(speed)
                    this.location = this.location.add(stepVector)
                    logger.info("移动中，已到 - ${this.location}")
                    ActionResult(
                        action, false
                    )
                }
            }

            is LoadAction -> {
                require(action.location == this.location)
                require(this.carrying == null)
                this.carrying = action.obj
                ActionResult(action, true).also {
                    logger.info("载入对象 - ${action.obj}")
                }
            }

            is UnloadAction -> {
                require(action.location == this.location)
                require(this.carrying != null)
                val obj = this.carrying
                this.carrying = null
                ActionResult(action, true).also {
                    logger.info("卸载对象 - $obj")
                }
            }

            is TaskStateUpdate -> {
                logger.info("任务执行完成 - ${action.taskId}")
                ActionResult(action, true)
            }

            else -> error("not supported")
        }

    }


}
