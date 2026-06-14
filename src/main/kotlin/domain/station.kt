package xyz.nietongxue.cfood.domain

interface Station {
    fun accept(operation: Operation): Boolean
    val location: Location
}

class Stove(val name: String, override val location: Location) : Station {
    override fun accept(operation: Operation): Boolean {
        return true
    }
}

