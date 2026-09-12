package xyz.nietongxue.cfood

import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.World
import java.time.Duration
import java.util.concurrent.ScheduledFuture


@Service
class Universe(
    val world: World,
    val taskScheduler: TaskScheduler,
    val universeConfig: UniverseConfig
) {
    val timeStep: Int = universeConfig.timeStep

    private var scheduledFuture: ScheduledFuture<*>? = null

    fun start() {
        world.setupMap((javaClass.getResource("/map.json")!!.readText()))
        world.setupProducts((javaClass.getResource("/products.json")!!.readText()))

        if (scheduledFuture == null || scheduledFuture!!.isDone) {
            scheduledFuture = taskScheduler.scheduleAtFixedRate(
                { world.tick() },
                Duration.ofMillis(timeStep.toLong())
            )
        }
    }

    fun stop() {
        scheduledFuture?.cancel(false)
        scheduledFuture = null
    }
}


@Component
class UniverseConfig(

){
    // 后台步长
    // 跟广播频率有关系，后台步长跟广播频率相同时最顺滑。
    // 如果关播频率大于步长，比如 2 倍。
    // 则相当于丢掉一个步长，此时获得的 speed 是其中一个步长的 speed，不是两个步长的 speed。

    val timeStep: Int = 500
    val broadcastStep:Int = 500
}