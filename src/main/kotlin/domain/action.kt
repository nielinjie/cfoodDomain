package xyz.nietongxue.cfood.domain

import arrow.core.Either
import org.slf4j.LoggerFactory
import xyz.nietongxue.common.response.ResponseChainResult

interface Action {
}//state, result

//当 finished = true，renewAction 被忽略。否则，renewAction 被塞在 queue 的第一个。
class ActionResult(val renewAction: Action, val isFinished: Boolean)

class ActionGroup(val seq: List<Action>, exceptionAction: Action) { //TODO 支持按组运行，没有应用。
    val logger = LoggerFactory.getLogger(this::class.java)!!
    var currentIndex: Int? = null
    fun current(): Action? = currentIndex?.let { seq[it] }

}

interface Actor {
    val queue: MutableList<Action>
    val history: MutableList<Action>
    val actCapabilities: List<ActCapability>
    fun current(): Action? = queue.firstOrNull()
    fun accept(vararg action: Action) {
        queue.addAll(action)
    }

    /**
     * 在执行过程中，可以插入新的动作。
     * act 可以更新队列。
     */
    fun insert(vararg action: Action) {
        if (queue.isEmpty())
            queue.addAll(action)
        else
            queue.addAll(1, action.toList())
    }

    fun tick() {
        if (this is TaskWatcher) {
            this.tock()
        }
        val act = current() ?: return
        this.doOneAction(act).also { //当 finished = true，renewAction 被忽略。否则，renewAction 被塞在 queue 的第一个。

            if (it.isFinished) {
                history.add(it.renewAction)
                queue.removeAt(0)
            } else {
                queue[0] = it.renewAction
            }
        }
    }

    fun doOneAction(action: Action): ActionResult {
        for (worker in actCapabilities) {
            when (val result = worker.act(action)) {
                is Either.Left<*> -> continue
                is Either.Right<*> -> return result.value as ActionResult
            }
        }
        error("no worker for $action")
    }

    val id: String


}

abstract class ActCapability {
    abstract fun act(action: Action): Either<ResponseChainResult.NotMe, ActionResult>
}