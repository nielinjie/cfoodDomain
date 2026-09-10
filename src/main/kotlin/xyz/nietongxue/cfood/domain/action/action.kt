package xyz.nietongxue.cfood.domain.action

import arrow.core.Either
import org.slf4j.LoggerFactory

/**
 * 对 action queue 的操作。
 * 一个 action 可以导致 queue 发生变化，比如替换队首、消费队首、追加到队尾、移动到队尾、循环监听。
 * 体现的是 action 对后续 action 的影响。
 */
interface ActionOperation {
    fun doWithQueue(queue: MutableList<Action>)

    /**
     * 更新自己。一般用在一个动作正常进行，没有完成，继续干。
     * 比如移动。没到目的地，继续。
     */
    data class ReplaceHead(val actions: List<Action>) : ActionOperation {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.removeFirst()
            queue.addAll(0, actions)
        }

        constructor(vararg actions: Action) : this(actions.toList())
    }

    /**
     * 消费自己，一般用在一个动作正常完成。
     */
    data object Consume : ActionOperation {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.removeFirst()
        }
    }

    data class Append(val actions: List<Action>) : ActionOperation {
        constructor(vararg actions: Action) : this(actions.toList())

        override fun doWithQueue(queue: MutableList<Action>) {
            queue.addAll(actions)
        }
    }

    /**
     * 移动到队尾，一般用在一个周期性动作完成一次。等待下一个周期再执行。
     * 比如各种 listen。
     */
    data class MoveToEnd(val action: Action) : ActionOperation {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.removeAt(0)
            queue.add(action)
        }
    }

    /**
     * 循环监听，将 listenAction 插入到 toActions 后面。
     * 一般用在“找 task”模式 -
     *  1. 找 task，
     *  2. task 被分解为解决动作。
     *  3. 继续找。
     */
    data class ListenLoop(val toActions: List<Action>, val listenAction: Action) : ActionOperation {
        override fun doWithQueue(queue: MutableList<Action>) {
            queue.addAll(toActions + listenAction)
            queue.removeAt(0)
        }
    }
}

interface Action

data class ActionResult(val effects: List<ActionOperation>) {
    constructor(vararg effects: ActionOperation) : this(effects.toList())
}

class ActionGroup(val seq: List<Action>, exceptionAction: Action) { //TODO 支持按组运行，没有应用。
    val logger = LoggerFactory.getLogger(this::class.java)!!
    var currentIndex: Int? = null
    fun current(): Action? = currentIndex?.let { seq[it] }

}

/**
 * 使用 action 实现动作的对象。
 */
abstract class Actor(
    val id: String
) {
    val queue: MutableList<Action> = mutableListOf()
    val history: MutableList<Action> = mutableListOf()

    abstract val abilities: List<Ability> //对 action的处理逻辑，体现了 actor 的能力。
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
        for (worker in abilities) {
            when (val result = worker.act(action)) {
                is Either.Left<*> -> continue
                is Either.Right<*> -> return result.value as ActionResult
            }
        }
        error("no worker for $action")
    }


}
