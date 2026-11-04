package xyz.nietongxue.cfood.domain.carry

import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.action.Action
import xyz.nietongxue.cfood.domain.station.Station
import xyz.nietongxue.common.base.Id


data class LoadAction(val location: Location, val obj: Id) : Action
data class UnloadAction(val location: Location) : Action

data class TransformAction(val location: Location, val obj: Id, val accepter: Station) : Action

interface Carriable {
    var carrying: String?
}


