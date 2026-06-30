package xyz.nietongxue.cfood

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.nietongxue.cfood.domain.BOMService
import xyz.nietongxue.cfood.domain.LogisticService
import xyz.nietongxue.cfood.domain.ObjectService
import xyz.nietongxue.cfood.domain.OrchestrateService
import xyz.nietongxue.cfood.domain.OrderService
import xyz.nietongxue.cfood.domain.ProductService
import xyz.nietongxue.cfood.domain.RoutingService
import xyz.nietongxue.cfood.domain.World
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
    fun logisticService(): LogisticService {
        return LogisticService()
    }

    @Bean
    fun productService(): ProductService {
        return ProductService()
    }

    @Bean
    fun objectService(productService: ProductService): ObjectService {
        return ObjectService(productService = productService)
    }

    @Bean
    fun routingService(): RoutingService {
        return RoutingService()
    }

    @Bean
    fun bomService(productService: ProductService): BOMService {
        return BOMService(productService = productService)
    }

    @Bean
    fun orchestrateService(
        productService: ProductService,
        routingService: RoutingService,
        bomService: BOMService,
        orderService: OrderService
    ): OrchestrateService {
        return OrchestrateService(
            productService = productService,
            routingService = routingService,
            bomService = bomService,
            orderService = orderService
        ).also {
            orderService.listener = it
        }
    }

    @Bean
    fun orderService(
        productService: ProductService,
        routingService: RoutingService,
        bomService: BOMService
    ): OrderService {
        return OrderService(
            productService = productService,
            routingService = routingService,
            bomService = bomService
        )
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
            it.init()
        }
    }

}