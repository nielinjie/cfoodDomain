package xyz.nietongxue.cfood.domain.facility

import xyz.nietongxue.cfood.domain.HasLocation
import xyz.nietongxue.cfood.domain.Position
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.HasName

interface Facility : HasId, HasLocation, HasName, HasSize,
    HasPorts //端口，设施上的分区，比如座位，输入点等。


interface HasSize {
    val size: Size
}

interface HasPorts {
    val ports: List<Port>
}

data class Port(val refPosition: Position, val name: String) {
    fun position(facility: Facility): Position {
        return Position(facility.location.position().x + refPosition.x, facility.location.position().y + refPosition.y)
    }
}

data class Size(
    val width: Int,
    val height: Int
)