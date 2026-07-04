package xyz.nietongxue.cfood.domain.facility

import xyz.nietongxue.cfood.domain.HasLocation
import xyz.nietongxue.cfood.domain.Position
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.HasName

interface Facility : HasId, HasLocation, HasName, HasSize, HasPorts


interface HasSize {
    val size: Size
}

interface HasPorts {
    val ports: List<Port>
}

data class Port(val position: Position, val name: String)

data class Size(
    val width: Int,
    val height: Int
)