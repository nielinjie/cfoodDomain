package xyz.nietongxue.cfood.domain

fun <T : Any> MutableList<T>.replace(element: T, newElement: T) {
    val index = indexOf(element)
    if (index != -1) {
        set(index, newElement)
    }
}