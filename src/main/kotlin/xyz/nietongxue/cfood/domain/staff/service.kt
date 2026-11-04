package xyz.nietongxue.cfood.domain.staff

import org.springframework.stereotype.Service
import xyz.nietongxue.cfood.domain.move.Movable
import xyz.nietongxue.common.base.HasId


interface Staff: Movable, HasId


@Service
class StaffService{
    val staff = mutableListOf<Staff>()
}