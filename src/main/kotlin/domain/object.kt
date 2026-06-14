package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id

@Component
class Shop(
    val objectService: ObjectService
) {
}


class Object(override val id: Id, val productId: Id) : HasId {}

@Service
class ObjectService(
    val productService: ProductService
) {

    val objects = mutableListOf<Object>()
    val states = mutableMapOf<Id, ObjectState>()
    val locations = mutableMapOf<Id, Location>()
    fun getByProductId(productId: Id): List<Object> {
        return objects.filter { it.productId == productId }
    }

    fun lock(objectId: Id, owner: Id): Object {
        val obj = objects.find { it.id == objectId }
        if (obj == null) {
            throw Exception("object not found")
        }
        if (states[objectId] != ObjectState.Waiting) {
            throw Exception("object is not waiting")
        }
        states[objectId] = ObjectState.Locked(owner)
        return obj
    }

    fun release(objectId: Id, owner: Id) {
        val obj = objects.find { it.id == objectId }
        if (obj == null) {
            throw Exception("object not found")
        }
        if (states[objectId] != ObjectState.Locked(owner)) {
            throw Exception("object is not locked by this owner")
        }
        states[objectId] = ObjectState.Waiting
    }

    fun moveTo(objectId: Id, location: Location) {
        val obj = objects.find { it.id == objectId }
        if (obj == null) {
            throw Exception("object not found")
        }
        locations[obj.id] = location
    }

}

interface ObjectState {
    object Waiting : ObjectState
    data class Locked(val owner: Id) : ObjectState
}