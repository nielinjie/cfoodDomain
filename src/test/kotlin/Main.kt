package xyz.nietongxue.cfood

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.nietongxue.cfood.domain.World
import xyz.nietongxue.cfood.domain.logistic.LogisticService
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.OrchestrateService
import xyz.nietongxue.cfood.domain.OrderService
import xyz.nietongxue.cfood.domain.path.LocalMap

@SpringBootApplication(scanBasePackages = ["xyz.nietongxue.cfood"])
class Main {
}


@Configuration
class Config {

    @Bean
    fun localMap(): LocalMap {
        return LocalMap(10, 10)
    }

    @Bean
    fun world(
        logisticService: LogisticService,
        objectService: ObjectService,
        orchestrateService: OrchestrateService,
        orderService: OrderService,
        localMap: LocalMap
    ): World {
        return World(
            logisticService = logisticService,
            objectService = objectService,
            orchestrateService = orchestrateService,
            orderService = orderService,
            localMap = localMap,
        ).also {
//            it.init()
        }
    }

}
