package xyz.nietongxue.cfood.domain.path

import xyz.nietongxue.cfood.domain.Position
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt


class Point(val x: Int, val y: Int)


/** 寻路配置  */
class Config {
    var allowDiagonal: Boolean = false // 是否允许斜角
    var heuristic: Heuristic = Heuristic.MANHATTAN //距离算法
}

enum class Heuristic {
    /**
     * 曼哈顿距离, 计算方式： |x1 - x2| + |y1 - y2|
     */
    MANHATTAN {
        override fun calc(x1: Int, y1: Int, x2: Int, y2: Int): Int {
            return abs(x1 - x2) + abs(y1 - y2)
        }
    },

    /**
     * 欧几里得距离, 计算方式： sqrt((x1 - x2)^2 + (y1 - y2)^2),
     */
    EUCLIDEAN {
        override fun calc(x1: Int, y1: Int, x2: Int, y2: Int): Int {
            val dx = (x1 - x2).toDouble()
            val dy = (y1 - y2).toDouble()
            return sqrt(dx * dx + dy * dy).toInt()
        }
    };

    abstract fun calc(x1: Int, y1: Int, x2: Int, y2: Int): Int
}

class PathFinder(val map: GameMap, val config: Config = Config()) {

    fun findPath(start: Position, end: Position): List<Position> {
        return findPath(start.x, start.y, end.x, end.y).let {
            it.map {
                Position(it.x, it.y)
            }
        }
    }


    /**
     * 对外 API：返回路径（包含起点）
     */
    fun findPath(startX: Int, startY: Int, endX: Int, endY: Int): List<Point> {
        if (!map.isWalkable(endX, endY)) {
            return listOf()
        }

        val openList = PriorityQueue<PathNode>()
        val closedSet: MutableSet<PathNode> = HashSet<PathNode>()

        val start = PathNode(startX, startY)
        start.g = 0
        start.h = config.heuristic.calc(startX, startY, endX, endY)
        openList.add(start)

        val dirs: Array<IntArray> = if (config.allowDiagonal) DIAGONAL_DIRS else STRAIGHT_DIRS

        while (!openList.isEmpty()) {
            val current = openList.poll()

            if (current.x == endX && current.y == endY) {
                return buildPath(current)
            }

            closedSet.add(current)

            for (d in dirs) {
                val nx = current.x + d[0]
                val ny = current.y + d[1]

                if (!map.isWalkable(nx, ny)) continue

                val neighbor = PathNode(nx, ny)
                if (closedSet.contains(neighbor)) continue

                val tentativeG = current.g + map.getCost(nx, ny)

                val existing = findInOpen(openList, neighbor)
                if (existing == null || tentativeG < existing.g) {
                    neighbor.g = tentativeG
                    neighbor.h = config.heuristic.calc(nx, ny, endX, endY)
                    neighbor.parent = current

                    if (existing == null) {
                        openList.add(neighbor)
                    }
                }
            }
        }
        return mutableListOf()
    }

    private fun buildPath(node: PathNode?): MutableList<Point> {
        var node = node
        val path = LinkedList<Point>()
        while (node != null) {
            path.addFirst(Point(node.x, node.y))
            node = node.parent
        }
        return path
    }

    private fun findInOpen(openList: PriorityQueue<PathNode>, node: PathNode): PathNode? {
        for (n in openList) {
            if (n.equals(node)) return n
        }
        return null
    }

    // 点结构（游戏逻辑使用）


    companion object {
        private val STRAIGHT_DIRS = arrayOf(
            intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1)
        )

        private val DIAGONAL_DIRS = arrayOf<IntArray>(
            intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1),
            intArrayOf(-1, -1), intArrayOf(-1, 1), intArrayOf(1, -1), intArrayOf(1, 1)
        )
    }
}

internal open class PathNode(val x: Int, val y: Int) : Comparable<PathNode> {
    var g: Int = 0
    var h: Int = 0
    var parent: PathNode? = null

    fun f(): Int {
        return g + h
    }

    override fun compareTo(o: PathNode): Int {
        return this.f().compareTo(o.f())
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is PathNode) return false
        val node = o
        return x == node.x && y == node.y
    }

    override fun hashCode(): Int {
        return 31 * x + y
    }
}