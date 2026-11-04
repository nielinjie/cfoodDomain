package xyz.nietongxue.cfood.visible

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import xyz.nietongxue.cfood.UniverseConfig
import xyz.nietongxue.cfood.domain.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap


@RestController
class VisibleController(
    val visibleService: VisibleService, val world: World,
    val universeConfig: UniverseConfig
) {

    val sse: MutableMap<String, SseEmitter> = ConcurrentHashMap()

    init {
        Thread {
            while (true) {
                broadcast()
                runCatching { Thread.sleep(universeConfig.broadcastStep.toLong()) }
            }
        }.start()
    }

    @GetMapping("/world/snapshot")
    fun snapshot(): SnapShot {
        return visibleService.snapshot(Rectangle(Position.zero, Size(400, 300)))
    }

    @PostMapping("/order")
    fun order() {
        with(world) {
            val order = Order(
                code = "TOMATO_EGG_ORDER", lines = listOf(
                    OrderLine(productCode = "TOMATO_EGG", quantity = 3)
                ), requiredTime = LocalDateTime.now().plusHours(12), state = OrderState.Waiting
            )
            orderService.accept(order)
        }
    }

    @GetMapping("/world/snapshot/stream", produces = ["text/event-stream"])
    fun snapshotStream(@RequestParam id: String): SseEmitter {
        val existing = sse[id]
        if (existing != null) {
            return existing
        }

        val emitter = SseEmitter(0L)

        emitter.onCompletion {
            sse.remove(id)
        }
        emitter.onTimeout {
            sse.remove(id)
        }
        emitter.onError { _ ->
            sse.remove(id)
        }

        sse[id] = emitter
        return emitter
    }

    private fun broadcast() {
        val snapshot = visibleService.snapshot(Rectangle(Position.zero, Size(400, 300)))
        sse.values.forEach { emitter ->
            try {
                emitter.send(snapshot)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
