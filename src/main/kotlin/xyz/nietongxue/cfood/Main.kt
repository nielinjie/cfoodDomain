package xyz.nietongxue.cfood

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.map.SimpleMap

@SpringBootApplication(scanBasePackages = ["xyz.nietongxue.cfood"])
class Main {
}


fun main() {
    runApplication<Main>()
}

@Component
class ApplicationLifecycleListener(
    val universe: Universe
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(event: ApplicationReadyEvent) {
        println("ApplicationContext is ready: ${event.applicationContext}")
        universe.start()
        with(universe.world) {
            objectService.inputByCode("TOMATO", 10, Location.XY(200, 200))
            objectService.inputByCode("EGG", 10, Location.XY(200, 200))
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosed(event: ContextClosedEvent) {
        println("ApplicationContext is about to be destroyed: ${event.applicationContext}")
        universe.stop()
    }
}


@Configuration
class Config {

    @Bean
    fun localMap(): SimpleMap {
        return SimpleMap(10, 10)
    }

    @Bean
    fun taskScheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.setPoolSize(1)
        scheduler.setThreadNamePrefix("universe-")
        scheduler.setRemoveOnCancelPolicy(true)
        scheduler.initialize()
        return scheduler
    }

}
