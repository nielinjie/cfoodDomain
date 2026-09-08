package xyz.nietongxue.cfood.domain.patron

import xyz.nietongxue.cfood.domain.Movable
import xyz.nietongxue.common.base.HasId


/**
 * 食客
 */
interface Patron : HasId, Movable, Fighting, Satisfy {
}

interface Fighting

interface Satisfy {
}