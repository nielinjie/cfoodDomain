package xyz.nietongxue.cfood.domain.facility

import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.HasLocation
import xyz.nietongxue.cfood.domain.Position
import xyz.nietongxue.cfood.domain.Size
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.HasName
import xyz.nietongxue.common.base.Id

interface Facility : HasId, HasLocation, HasName, HasSize,
    HasPorts //端口，设施上的分区，比如座位，输入点等。


interface HasSize {
    val size: Size
}

interface HasPorts {
    val ports: List<Port>
}

data class Port(val refPosition: Position, val name: String, val facility: Facility) {
    fun position(): Position {
        return Position(facility.location.position().x + refPosition.x, facility.location.position().y + refPosition.y)
    }

    fun idString(): String {
        return "${facility.id}-${name}"
    }
}


@Service
class HouseService(

) {

    fun findSeat(hasLocation: HasLocation, strategy: SeatFindingStrategy = SeatFindingStrategy.RandomSeatFindingStrategy): Port? {
        return facilities.filterIsInstance<Table>().flatMap { it.ports }.firstOrNull { port ->
            seats[port] == null
        }
    }
    fun seatAvailable(port: Port): Boolean {
        return seats[port] == null
    }

    fun takeSeat(port: Port, id: Id) {
        require(seats[port] == null || seats[port] == id)
        seats[port] = id
    }

    fun unTakeSeat(port: Port) {
        seats.remove(port)
    }

    val facilities: MutableList<Facility> = mutableListOf()
    val seats: MutableMap<Port, Id> = mutableMapOf()
}

interface SeatFindingStrategy {
    data object RandomSeatFindingStrategy : SeatFindingStrategy
    data object NearestSeatFindingStrategy : SeatFindingStrategy
}
