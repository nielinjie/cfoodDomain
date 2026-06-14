package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7

@Service
class LocationService {
    val objectLocations: MutableMap<String, Location> = mutableMapOf()
    fun getLocation(objectId: Id): Location? {
        return objectLocations[objectId]
    }

    fun getByName(name: String): Location? {
        return objectLocations.values.firstOrNull { it is Location.NamedLocation && it.name == name }
    }
}

interface Location {
    data class NamedLocation(val name: String) : Location

}

class Object(override val id: Id, val productId: Id) : HasId {}

@Service
class ObjectService(
    val productService: ProductService
) {
    val objects = mutableListOf<Object>()
    fun getByProductId(productId: Id): List<Object> {
        return objects.filter { it.productId == productId }
    }
}

class LogisticTask(
    override val id: Id = v7(),
    val productId: Id,
    val quantity: Int,
    val dest: Location
) : HasId

interface LogisticTaskState {
    object Waiting : LogisticTaskState
    data class Processing(val carryId: Id, val objectIds: List<Id>) : LogisticTaskState
    object Finished : LogisticTaskState
}

@Service
class LogisticService {
    typealias TaskAndState = Pair<LogisticTask, LogisticTaskState>

    val logisticTasks = mutableListOf<TaskAndState>()
    fun logisticRequest(productId: String, quantity: Int, dest: Location) {
        logisticTasks.add(
            LogisticTask(
                productId = productId,
                quantity = quantity,
                dest = dest
            ) to LogisticTaskState.Waiting
        )
    }

    fun dispatch(carryId: Id): LogisticTask? {
        return logisticTasks.firstOrNull { it.second is LogisticTaskState.Waiting }.also {
            if (it != null) {
                logisticTasks.remove(it)
                logisticTasks.add(it.first to LogisticTaskState.Processing(carryId, emptyList()))
            }
        }?.first
    }

    fun finish(task: LogisticTask) {
        logisticTasks.removeIf { it.first == task }
        logisticTasks.add(task to LogisticTaskState.Finished)
    }
}