package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.HasId
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.Name
import xyz.nietongxue.common.base.v7

data class Product(
    override val id: Id = v7(),
    val name: String, val code: String,
    val type: ProductType
) : HasId {
    val unit: String = ""
}

enum class ProductType { //成品、半成品、原料
    FINISHED,
    INTERMEDIATE,
    RAW
}


@Service
class ProductService {
    val products = mutableListOf<Product>()
    fun getById(id: Id): Product? {
        return products.find { it.id == id }
    }
    fun getByName(name: Name): Product? {
        return products.find { it.name == name }
    }

    fun save(product: Product) {
        products.add(product)
    }

}
