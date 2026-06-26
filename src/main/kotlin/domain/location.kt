package xyz.nietongxue.cfood.domain




data class Position(val x: Int, val y: Int) {

    /**
     * 减去另一个点 b ，结果就是从b 到 this 的向量
     */
    fun minus(position: Position): Vector {
        return Vector(x - position.x, y - position.y)
    }

    fun add(vector: Vector): Position {
        return Position(x + vector.x, y + vector.y)
    }
}

/**
 * 向量，一个最简单的路径规划，从一个点到另一个点的直线。
 */
data class Vector(val x: Int, val y: Int) {
    fun step(speed: Int): Vector {
        require(speed > 0)
        val xDirection = true
        val yDirection = !xDirection || x == 0
        val newX = if (xDirection) {
            when {
                (x > 0) -> speed
                (x == 0) -> 0
                else -> -speed
            }
        } else 0
        val newY = if (yDirection) {
            when {
                (y > 0) -> speed
                (y == 0) -> 0
                else -> -speed
            }
        } else 0
        return Vector(newX, newY)
    }
}


interface Location {
//    data class NamedLocation(val name: String) : Location {
//        override fun position(): Position {
//            TODO("Not yet implemented")
//        }
//    }

    data class XY(val x: Int, val y: Int) : Location {
        override fun position(): Position {
            return Position(x, y)
        }

        constructor(position: Position) : this(position.x, position.y)
    }

    fun position(): Position
    fun add(vector: Vector): Location {
        return XY(position().add(vector))
    }

    fun minus(location: Location): Vector {
        return this.position().minus(location.position())
    }

    fun samePosition(location: Location): Boolean {
        return this.position() == location.position()
    }

}


