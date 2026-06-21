package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory.getLogger


class MoveTest {
    val logger = getLogger(this::class.java)!!

    @Test
    fun move() {
        val carrier = Carrier()
        carrier.accept(
            MoveAction(Location.XY(3, 2)),
            LoadAction(Location.XY(3, 2), "obj1"),
            MoveAction(Location.XY(6, 1)),
            UnloadAction(Location.XY(6, 1)), TaskStateUpdate("task1")
        )

        for (i in 1..15) {
            carrier.tick()
            Thread.sleep(200)
        }
    }
}