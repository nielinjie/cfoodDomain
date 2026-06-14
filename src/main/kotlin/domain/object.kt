package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Component

@Component
class Shop(
    val locationService: LocationService
) {
    val objects = mutableListOf<Object>()
}



