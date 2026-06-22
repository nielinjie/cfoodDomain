package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.PathFinder

class PathFinderTest {
    val map = object : GameMap {
        override val width: Int
            get() = 5
        override val height: Int
            get() = 5

        override fun isWalkable(x: Int, y: Int): Boolean {
            return true
        }

    }

    @Test
    fun findPath() {
        val pathFinder = PathFinder(map)
        val path = pathFinder.findPath(Location.XY(0, 0).position(), Location.XY(9, 9).position())
        println(path)
    }
}