package xyz.nietongxue.cfood.domain

import org.springframework.stereotype.Service
import xyz.nietongxue.common.base.Id



interface Location {
    data class NamedLocation(val name: String) : Location

}