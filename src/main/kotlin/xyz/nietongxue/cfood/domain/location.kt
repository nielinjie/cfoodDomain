package xyz.nietongxue.cfood.domain


data class Position(val x: Int, val y: Int)


interface Location {
    data class XY(val x: Int, val y: Int) : Location {
        override fun position(): Position {
            return Position(x, y)
        }

        constructor(position: Position) : this(position.x, position.y)
    }

    fun position(): Position


    fun samePosition(location: Location): Boolean {
        return this.position() == location.position()
    }

}


