package org.rsmod.game.action

import kotlin.reflect.KClass

class ActionBus(
    val mappedExecutors: MutableMap<KClass<out Action>, ActionExecutorMap> = mutableMapOf(),
    val listExecutors: MutableMap<KClass<out Action>, MutableList<ActionExecutor<*>>> = mutableMapOf()
) {

    inline fun <reified T : Action> register(id: Long, noinline executor: ActionExecutor<T>): Boolean {
        val executors = mappedExecutors.getOrPut(T::class) { ActionExecutorMap() }
        return executors.register(id, executor)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Action> publish(action: T, id: Long): Boolean {
        val executors = mappedExecutors[action::class] ?: return false
        val executor = executors[id] as? ActionExecutor<T> ?: return false
        executor(action)
        return true
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Action> publish(action: T, id: Int) = publish(action, id.toLong())

    inline fun <reified T : Action> register(noinline executor: ActionExecutor<T>) {
        val list = listExecutors.getOrPut(T::class) { mutableListOf() }
        list.add(executor)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Action> publish(action: T): Boolean {
        val executors = listExecutors[action::class] as? List<ActionExecutor<T>> ?: return false
        executors.forEach { it(action) }
        return executors.isNotEmpty()
    }
}
