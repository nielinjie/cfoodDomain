package xyz.nietongxue.cfood.domain.facility

import xyz.nietongxue.cfood.domain.Location
import xyz.nietongxue.cfood.domain.Size
import xyz.nietongxue.common.base.Id
import xyz.nietongxue.common.base.Name
import xyz.nietongxue.common.base.v7

class Storage(
    override val id: Id = v7(),
    override val location: Location,
    override val name: Name,
    override val size: Size,
    override val ports: List<Port>
) : Facility {
}