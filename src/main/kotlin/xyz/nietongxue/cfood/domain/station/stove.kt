package xyz.nietongxue.cfood.domain.station

import org.slf4j.LoggerFactory
import xyz.nietongxue.cfood.domain.*
import xyz.nietongxue.cfood.domain.facility.Port
import xyz.nietongxue.cfood.domain.facility.Size
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.operation.OperateAbility
import xyz.nietongxue.cfood.domain.operation.OperationTask
import xyz.nietongxue.cfood.domain.operation.OperationTaskHandle
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7

/**
 * 灶台
 * 一种加工工作站。负责一种加工。
 */
class Stove(
    override val name: String,
     id: Id = v7(),
    override val size: Size,
    override val ports: List<Port>,
    override var location: Location,
    val orchestrateService: OrchestrateService,
    val objectService: ObjectService,
    val logisticService: LogisticService,
    val world: World
) : Actor(id),Station, OperationTaskHandle {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    override var task: OperationTask? = null


    fun init() {
        queue.add(CheckTaskAction)
    }



    override val abilities: List<Ability> = listOf(
        OperateAbility(
            this@Stove,
            this@Stove,
            this@Stove,
            logisticService,
            objectService,
            orchestrateService = orchestrateService,
            world
        )
    )
}

