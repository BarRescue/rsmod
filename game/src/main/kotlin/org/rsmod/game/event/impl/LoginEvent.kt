package org.rsmod.game.event.impl

import org.rsmod.game.event.Event
import org.rsmod.game.model.mob.Player

data class LoginEvent(
    val player: Player,
    val priority: Priority
) : Event {

    sealed class Priority {
        object High : Priority()
        object Normal : Priority()
        object Low : Priority()
    }
}

data class LogoutEvent(
    val player: Player
) : Event
