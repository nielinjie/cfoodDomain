package xyz.nietongxue.cfood.domain

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7


data class LoadAction(val location: Location, val obj: Id) : Action
data class UnloadAction(val location: Location) : Action
data class TaskStateUpdate(val taskId: String) : Action
data class CheckTaskAction(val taskType: String = "Logistic") : Action

@Component
@Scope("prototype")
class Carrier(
    val objectService: ObjectService,
    val logisticService: LogisticService,
    @param:Autowired(required = false)
    val id: Id = v7(),
) : Actor() {
    val logger = getLogger(this::class.java)!!
    var location: Location = Location.XY(0, 0)
    val speed: Int = 1
    var carrying: String? = null
    var task: LogisticTask? = null


    @PostConstruct
    fun init() {
        this.accept(CheckTaskAction())
    }


    fun LogisticTask.toActions(): List<Action> {
        val obj: Id = objectService.getOneFree(this.productId)!!.id
        val objLocation: Location = objectService.getLocation(obj)
        return listOf(
            MoveAction(objLocation),
            LoadAction(objLocation, obj),
            MoveAction(this.dest),
            UnloadAction(this.dest),
            TaskStateUpdate(this.id)
        )
    }

    fun accept(task: LogisticTask) {
        require(this.task == null)
        this.task = task
        logger.info("接受任务 - $task")
        this.accept(*task.toActions().toTypedArray())
    }

    override fun doIt(action: Action): ActionResult {
        return when (action) {
            is MoveAction -> {
                if (this.location.samePosition(action.dest)) ActionResult(action, true).also {
                    logger.info("到达目的地 - ${action.dest}")
                }
                else {
                    val stepVector = (action.dest.minus(this.location)).step(speed)
                    this.location = this.location.add(stepVector)
                    if (this.carrying != null)
                        objectService.setLocation(this.carrying!!, this.location, this.id)
                    logger.info("移动中，已到 - ${this.location}")
                    ActionResult(
                        action, false
                    )
                }
            }

            is LoadAction -> {
                require(action.location.samePosition(this.location))
                require(this.carrying == null)
                require(objectService.getLocation(action.obj).samePosition(this.location))
                this.carrying = action.obj
                objectService.lock(action.obj, this.id)
                ActionResult(action, true).also {
                    logger.info("载入对象 - ${action.obj}")
                }
            }

            is UnloadAction -> {
                require(action.location.samePosition(this.location))
                require(this.carrying != null)
                val obj = this.carrying
                this.carrying = null
                objectService.release(obj!!, this.id)
                ActionResult(action, true).also {
                    logger.info("卸载对象 - $obj")
                }
            }

            is TaskStateUpdate -> {
                require(this.task != null)
                logger.info("任务执行完成 - ${action.taskId}")
                logisticService.finish(this.task!!)
                this.task = null
                this.accept(CheckTaskAction())
                ActionResult(action, true)
            }

            is CheckTaskAction -> {
                require(this.task == null)
                require(action.taskType == "Logistic")
                val task = logisticService.dispatch(this.id)

                if (task != null) {
                    logger.info("发现任务 - $task")
                    this.accept(task)
                    ActionResult(action, true)
                } else {
                    logger.info("无任务")
                    ActionResult(action, false)
                }
            }

            else -> error("not supported")
        }

    }


}
