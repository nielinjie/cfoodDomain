package xyz.nietongxue.cfood.domain

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7


@Component
@Scope(SCOPE_PROTOTYPE)
class Carrier(
    val objectService: ObjectService,
    val logisticService: LogisticService,
    @param:Autowired(required = false)
    override val id: Id = v7(),
    val localMap: GameMap,
) : Actor, TaskWatcher, Movable, Carriable, LogisticTaskHandle {

    val logger = LoggerFactory.getLogger(this::class.java)!!

    override var location: Location = Location.XY(0, 0)
    override var speed: Int = 1
    override var carrying: String? = null
    override var task: LogisticTask? = null


    override val queue: MutableList<Action> = mutableListOf()
    override val history: MutableList<Action> = mutableListOf()


    override val actCapabilities: List<ActCapability> = listOf(
        MoveCapability(this, objectService),
        CarryCapability(this, objectService),
        LogisticTaskWorkerCapability(this, logisticService)
    )

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

    override val taskWatchingList: List<TaskWatching> = listOf(
        object : TaskWatching {
            override fun pull() {
                if (this@Carrier.task != null) return
                val task = logisticService.dispatch(this@Carrier.id)
                if (task != null) {
                    this@Carrier.task = task
                    logger.info("接受任务 - $task")
                    accept(*task.toActions(this@Carrier).toTypedArray<Action>())
                }
            }
        }
    )


}
