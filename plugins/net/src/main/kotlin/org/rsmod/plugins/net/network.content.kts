package org.rsmod.plugins.net

import org.rsmod.game.event.GameBootUp
import org.rsmod.game.events.EventBus
import org.rsmod.plugins.api.net.platform.GamePlatformPacketMaps

private val events: EventBus by inject()

private val platformPackets: GamePlatformPacketMaps by inject()
events.subscribe<GameBootUp> { platformPackets.eagerInitialize() }
