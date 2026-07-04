package xyz.nietongxue.cfood.domain

import arrow.core.Either
import org.slf4j.LoggerFactory
import xyz.nietongxue.common.response.ResponseChainResult

interface ActionEffect {
    fun doWithQueue(queue: MutableList<Action>)
    data class ReplaceHead(val action: Action) : ActionEffect {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue[0] = action
        }
    }

    data object Consume : ActionEffect {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.removeAt(0)
        }
    }

    data class Append(val actions: List<Action>) : ActionEffect {
        constructor(vararg actions: Action) : this(actions.toList())

        override fun doWithQueue(queue: MutableList<Action>) {
            queue.addAll(actions)
        }
    }

    data class MoveToEnd(val action: Action) : ActionEffect {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.removeAt(0)
            queue.add(action)
        }
    }

    data class ListenLoop(val toActions: List<Action>, val listenAction: Action) : ActionEffect {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.addAll(toActions + listenAction)
            queue.removeAt(0)
        }
    }
}

interface Action {
}//state, result

data class ActionResult(val effects: List<ActionEffect>) {
    constructor(vararg effects: ActionEffect) : this(effects.toList())
}

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

    fun tick() {
        val act = current() ?: return
        this.doOneAction(act).also {
            it.effects.forEach {
                it.doWithQueue(queue)
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