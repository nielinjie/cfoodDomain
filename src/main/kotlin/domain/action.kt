package xyz.nietongxue.cfood.domain

import org.slf4j.LoggerFactory

interface Action {
}//state, result

class ActionResult(val renewAction: Action, val isFinished: Boolean)

class ActionGroup(val seq: List<Action>, exceptionAction: Action) { //TODO 支持按组运行，没有应用。
    val logger = LoggerFactory.getLogger(this::class.java)!!
    var currentIndex: Int? = null
    fun current(): Action? = currentIndex?.let { seq[it] }

}

abstract class Actor {
    val queue: MutableList<Action> = mutableListOf()
    val history: MutableList<Action> = mutableListOf()
    fun current(): Action? = queue.firstOrNull()
    fun accept(vararg action: Action) {
        queue.addAll(action)
    }


    fun tick() {
        val act = current() ?: return
        this.doIt(act).also {
            if (it.isFinished) {
                history.add(it.renewAction)
                queue.removeAt(0)
            } else {
                queue[0] = it.renewAction
            }
        }
    }

    abstract fun doIt(action: Action): ActionResult
}