package xyz.nietongxue.cfood.domain.logistic

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.Task
import xyz.nietongxue.cfood.domain.TaskState
import xyz.nietongxue.cfood.domain.TaskState.Finished
import xyz.nietongxue.cfood.domain.TaskState.Waiting
import xyz.nietongxue.cfood.domain.station.Station
import xyz.nietongxue.common.base.Id


class LogisticService(
) : xyz.nietongxue.cfood.domain.TaskManager {


    val logger: Logger = getLogger(this::class.java)

    val tasks = mutableListOf<LogisticTask>()
    val states = mutableMapOf<Id, TaskState>()
    fun logisticRequest(productId: String, quantity: Int, dest: Location) {
        val task = LogisticUnloadTask(productId = productId, quantity = quantity, dest = dest)
        tasks.add(task)
        states[task.id] = Waiting
        logger.info("logistic task created -  $task")
    }

    fun logisticTransformRequest(productId: String, quantity: Int, dest: Station) {
        val task = LogisticTransformTask(productId = productId, quantity = quantity, destStation = dest)
        tasks.add(task)
        states[task.id] = Waiting
        logger.info("logistic transform task created - $task")
    }

    override fun dispatch(acceptId: Id): LogisticTask? {
        return tasks.firstOrNull { task ->
            states[task.id] == Waiting
        }?.also { task ->
            states[task.id] = ProcessingLogistic(acceptId, listOf())
        }?.also {
            logger.info("dispatch task - $it")
        }
    }

    override fun finish(task: Task) {
        require(task is LogisticTask)
        states[task.id] = Finished
        logger.info("finish task - $task")
    }
}


interface TaskHandle<T:Task>  {
    var task: T?
}

