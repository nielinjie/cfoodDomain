package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.v7

class BOM(override val id: Id = v7(), val productId: Id, val lines: List<BOMLine>, val version: Version = Version(0)) :
    HasId

class BOMLine(override val id: Id = v7(), val componentId: Id, val quantity: Int) : HasId


class Version(val version: Int)


@Service
class BOMService(
    val productService: ProductService
) {
    val boms = mutableListOf<BOM>()
    fun getByProductId(productId: Id): BOM? {
        return boms.firstOrNull { it.productId == productId }
    }

    fun getComponents(productId: Id): List<BOMLine> {
        return getByProductId(productId)?.lines ?: error("not find")
    }

    fun save(bom: BOM) {
        boms.add(bom)
    }
}