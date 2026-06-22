package xyz.nietongxue.cfood.domain.path

interface GameMap {
    /** 地图宽度  */
    val width: Int

    /** 地图高度  */
    val height: Int

    /**
     * 是否可通行
     */
    fun isWalkable(x: Int, y: Int): Boolean

    /**
     * 移动消耗（默认 1，可扩展为地形代价）
     */
    fun getCost(x: Int, y: Int): Int {
        return 1
    }
}
class LocalMap(override val width: Int, override val height: Int) : GameMap {
    override fun isWalkable(x: Int, y: Int): Boolean {
        return true
    }

}