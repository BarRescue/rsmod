package org.rsmod.game.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val logger = InlineLogger()

class StartupTaskList internal constructor(
    internal val blocking: MutableList<StartupTask>,
    internal val nonBlocking: MutableList<StartupTask>
) {

    /* used for dependency injection */
    @Suppress("UNUSED")
    constructor() : this(mutableListOf(), mutableListOf())

    fun registerNonBlocking(block: () -> Unit) = nonBlocking.add(StartupTask(block))

    fun registerBlocking(block: () -> Unit) = blocking.add(StartupTask(block))
}

fun StartupTaskList.launchNonBlocking(scope: CoroutineScope) = runBlocking {
    logger.debug { "Executing non-blocking start up tasks (size=${nonBlocking.size})" }
    val ioJob = scope.launch {
        nonBlocking.forEach {
            launch { it.block() }
        }
    }
    ioJob.join()
}

fun StartupTaskList.launchBlocking() {
    logger.debug { "Executing blocking start up tasks (size=${blocking.size})" }
    blocking.forEach { it.block() }
}
