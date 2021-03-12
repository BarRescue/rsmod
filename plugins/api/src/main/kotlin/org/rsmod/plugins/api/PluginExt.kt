package org.rsmod.plugins.api

import org.rsmod.game.event.impl.CloseModal
import org.rsmod.game.event.impl.CloseOverlay
import org.rsmod.game.event.impl.CloseTopLevel
import org.rsmod.game.event.impl.LoginEvent
import org.rsmod.game.event.impl.OpenModal
import org.rsmod.game.event.impl.OpenOverlay
import org.rsmod.game.event.impl.OpenTopLevel
import org.rsmod.game.cmd.CommandBuilder
import org.rsmod.game.event.impl.LogoutEvent
import org.rsmod.game.model.obj.type.ObjectType
import org.rsmod.game.model.ui.Component
import org.rsmod.game.model.ui.UserInterface
import org.rsmod.game.plugin.Plugin
import org.rsmod.plugins.api.protocol.packet.ButtonClick
import org.rsmod.plugins.api.protocol.packet.ObjectAction

fun Plugin.onEarlyLogin(block: LoginEvent.() -> Unit) {
    onEvent<LoginEvent>()
        .where { priority == LoginEvent.Priority.High }
        .then(block)
}

fun Plugin.onLogin(block: LoginEvent.() -> Unit) {
    onEvent<LoginEvent>()
        .where { priority == LoginEvent.Priority.Normal }
        .then(block)
}

fun Plugin.onPostLogin(block: LoginEvent.() -> Unit) {
    onEvent<LoginEvent>()
        .where { priority == LoginEvent.Priority.Low }
        .then(block)
}

fun Plugin.onLogout(block: LogoutEvent.() -> Unit) {
    onEvent<LogoutEvent>().then(block)
}

fun Plugin.onCommand(cmd: String, block: CommandBuilder.() -> Unit) {
    commands.register(cmd, block)
}

fun Plugin.onOpenTopLevel(top: UserInterface, block: OpenTopLevel.() -> Unit) {
    onEvent<OpenTopLevel>()
        .where { this.top == top }
        .then(block)
}

fun Plugin.onCloseTopLevel(top: UserInterface, block: CloseTopLevel.() -> Unit) {
    onEvent<CloseTopLevel>()
        .where { this.top == top }
        .then(block)
}

fun Plugin.onOpenModal(modal: UserInterface, block: OpenModal.() -> Unit) {
    onEvent<OpenModal>()
        .where { this.modal == modal }
        .then(block)
}

fun Plugin.onCloseModal(modal: UserInterface, block: CloseModal.() -> Unit) {
    onEvent<CloseModal>()
        .where { this.modal == modal }
        .then(block)
}

fun Plugin.onOpenOverlay(overlay: UserInterface, block: OpenOverlay.() -> Unit) {
    onEvent<OpenOverlay>()
        .where { this.overlay == overlay }
        .then(block)
}

fun Plugin.onCloseOverlay(overlay: UserInterface, block: CloseOverlay.() -> Unit) {
    onEvent<CloseOverlay>()
        .where { this.overlay == overlay }
        .then(block)
}

fun Plugin.onButton(component: Component, block: ButtonClick.() -> Unit) {
    onAction(component.packed, block)
}

fun Plugin.onObject(obj: ObjectType, opt: String, block: ObjectAction.() -> Unit) {
    val option = obj.options.indexOfFirst { it != null && it.equals(opt, ignoreCase = false) }
    if (option == -1) {
        val ignoreCase = obj.options.firstOrNull { it != null && it.equals(opt, ignoreCase = true) }
        if (ignoreCase != null) {
            val errorMessage = "Letter case option error for object \"${obj.name}\" (id=${obj.id})"
            val foundMessage = "Found [\"$ignoreCase\"] but was given [\"$opt\"]"
            error("$errorMessage. $foundMessage.")
        }
        error("Option for object \"${obj.name}\" not found. (id=${obj.id}, option=$opt)")
    }
    when (option) {
        0 -> onAction<ObjectAction.Option1>(obj.id, block)
        1 -> onAction<ObjectAction.Option2>(obj.id, block)
        2 -> onAction<ObjectAction.Option3>(obj.id, block)
        3 -> onAction<ObjectAction.Option4>(obj.id, block)
        4 -> onAction<ObjectAction.Option5>(obj.id, block)
        else -> error("Unhandled object option. (obj=${obj.name}, id=${obj.id}, option=$option)")
    }
}
