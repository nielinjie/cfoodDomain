package xyz.nietongxue.cfood.domain

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import xyz.nietongxue.cfood.domain.path.GameMap
import xyz.nietongxue.cfood.domain.path.LocalMap
import xyz.nietongxue.common.base.stuff


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class LogisticTest(
    productService: ProductService,
    routingService: RoutingService,
    bomService: BOMService,
    val objectService: ObjectService,
    val applicationContext: ApplicationContext,
    val logisticService: LogisticService
) : BaseProducts(
    productService, routingService, bomService
) {
    val logger = getLogger(this::class.java)!!


    @TestConfiguration
    class Configure() {
        @Bean
        fun localMap(): GameMap {
            return LocalMap(10, 10)
        }
    }


    @Test
    fun autoGetTask() {
        val carrier = applicationContext.getBean<Carrier>()
        objectService.input(this.egg.id, 10, location = Location.XY(2, 4), stuff())
        logisticService.logisticRequest(this.egg.id, 1, Location.XY(6, 1))
        for (i in 1..30) {
            carrier.tick()
            Thread.sleep(100)
        }
    }

    @Test
    fun autoGetTaskStation() {
        val carrier = applicationContext.getBean<Carrier>()
        val station = applicationContext.getBean<Stove>().also {
            it.location = Location.XY(5, 5)
        }
        objectService.input(this.egg.id, 10, location = Location.XY(2, 4),stuff())
        logisticService.logisticTransformRequest(this.egg.id, 1, station)
        for (i in 1..30) {
            carrier.tick()
            Thread.sleep(100)
        }

    }

    /**
     * 有第二个任务，carrier 继续完成。
     */
    @Test
    fun autoGetTask2() {
        val carrier = applicationContext.getBean<Carrier>()
        objectService.input(this.egg.id, 10, location = Location.XY(2, 4),stuff())
        logisticService.logisticRequest(this.egg.id, 1, Location.XY(6, 1))
        for (i in 1..60) {
            carrier.tick()
            Thread.sleep(100)
            if (i == 30) {
                logisticService.logisticRequest(this.egg.id, 1, Location.XY(5, 3))
            }
        }
    }

    /**
     * 有第二个任务，在 carrier 还没完成时就出现了。carrier 完成第一个后继续。
     */
    @Test
    fun autoGetTask22() {
        //TODO 有个现象：carrier 把刚放下的东西拿走了。这个属于机制决定的正常现象。需要如果要避免，就不能是简单放下，而是一个转交。
        val carrier = applicationContext.getBean<Carrier>()
        objectService.input(this.egg.id, 10, location = Location.XY(2, 4),stuff())
        logisticService.logisticRequest(this.egg.id, 1, Location.XY(6, 1))
        for (i in 1..60) {
            carrier.tick()
            Thread.sleep(100)
            if (i == 10) {
                logisticService.logisticRequest(this.egg.id, 1, Location.XY(5, 3))
            }
        }
    }

    /**
     * 有第二个carrier
     */
    @Test
    fun autoGetTask3() {
        val carrier = applicationContext.getBean<Carrier>()
        val carrier2 = applicationContext.getBean<Carrier>()
        objectService.input(this.egg.id, 10, location = Location.XY(2, 4))
        logisticService.logisticRequest(this.egg.id, 1, Location.XY(6, 1))
        for (i in 1..60) {
            carrier.tick()
            carrier2.tick()
            Thread.sleep(100)
            if (i == 10) {
                logisticService.logisticRequest(this.egg.id, 1, Location.XY(5, 3))
            }
        }
    }

}
