package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id

@Component
class Shop(
    val locationService: LocationService
) {
    val objects = mutableListOf<Object>()
}

@Service
class LocationService {
    val objectLocations: MutableMap<String, Location> = mutableMapOf()
    fun getLocation(objectId: Id): Location? {
        return objectLocations[objectId]
    }
}

interface Location

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