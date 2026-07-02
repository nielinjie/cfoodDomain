package xyz.nietongxue.cfood.domain

import xyz.nietongxue.common.base.*

/**
 * 可以占有 obj 的东西。比如设备、车辆。
 * 仓库应该不行。
 * 订单行不行？操作行不行？现在是不行，这里的占有是物理占有，不是计划占有。
 * 被物理占有了就不能被其他物理占有。
 */

interface Owner {
    val id: Id
}

class Object(override val id: Id, val productId: Id, val labels: MutableMap<String, Any> = mutableMapOf()) : HasId {
    fun label(key: String, value: Any) {
        labels[key] = value
    }

    fun label(key: String): Any? {
        return labels[key]
    }
}

class ObjectService(
    val productService: ProductService
) {

    val objects = mutableListOf<Object>()
    val states = mutableMapOf<Id, ObjectState>()
    val locations = mutableMapOf<Id, Location>()

    fun inputByCode(productCode: String, quantity: Int = 1, location: Location, labels: Stuff = stuff()) {
        val product = productService.getByCode(productCode)!!
        input(productId = product.id, quantity = quantity, location = location, labels = labels)
    }

    fun input(productId: Id, quantity: Int = 1, location: Location, labels: Stuff = stuff()) {
        for (i in 1..quantity) {
            val obj = Object(id = v7(), productId = productId, labels = labels.toMutableMap())
            objects.add(obj)
            states[obj.id] = ObjectState.Free
            locations[obj.id] = location
        }
    }

    fun product(productId: Id, quantity: Int = 1, location: Location, labels: Stuff) {
        input(productId, quantity, location, labels) //实际上是一样的，语义不同。
    }


    fun consume(objId: Id, ownerId: Id) {
        require(states[objId] == ObjectState.Locked(ownerId))
        states.remove(objId)
        locations.remove(objId)
        objects.removeIf { it.id == objId }
    }

    fun get(id: Id): Object? = objects.find { it.id == id }

    fun getByProductId(productId: Id): List<Object> {
        return objects.filter { it.productId == productId }
    }

    fun getByOwner(owner: Id): List<Object> {
        return objects.filter { states[it.id] == ObjectState.Locked(owner) }
    }

    fun getOneFree(productId: Id): Object? {
        return objects.find { it.productId == productId && states[it.id] == ObjectState.Free }
    }

    fun getLocation(objectId: Id): Location {
        return locations[objectId]!!
    }


    fun lock(objectId: Id, owner: Id): Object {
        val obj = objects.find { it.id == objectId }
        if (obj == null) {
            error("object not found")
        }
        if (states[objectId] != ObjectState.Free) {
            error("object is not waiting")
        }
        states[objectId] = ObjectState.Locked(owner)
        return obj
    }

    fun release(objectId: Id, owner: Id) {
        require(states[objectId] == ObjectState.Locked(owner)) {
            "object is not locked by this owner"
        }
        states[objectId] = ObjectState.Free
    }

    fun transfer(objectId: Id, to: Id, owner: Id) {
        require(states[objectId] == ObjectState.Locked(owner)) {
            "object is not locked by this owner"
        }
        states[objectId] = ObjectState.Locked(to)
    }

    fun setLocation(objectId: Id, location: Location, owner: Id) {
        require(states[objectId] == ObjectState.Locked(owner))
        locations[objectId] = location
    }

    fun output(objects: List<Object>) {
        for (obj in objects) {
            states.remove(obj.id)
            locations.remove(obj.id)
            this.objects.removeIf { it.id == obj.id }
        }
    }


}

interface ObjectState {
    object Free : ObjectState
    data class Locked(val owner: Id) : ObjectState
}