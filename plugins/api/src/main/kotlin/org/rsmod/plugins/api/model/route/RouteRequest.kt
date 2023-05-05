package org.rsmod.plugins.api.model.route

import org.rsmod.game.model.mob.move.MovementSpeed
import org.rsmod.game.model.route.RouteRequest
import org.rsmod.plugins.api.map.GameObject

public data class RouteRequestGameObject(
    public val destination: GameObject,
    public override val speed: MovementSpeed? = null,
    public override val async: Boolean = false
) : RouteRequest
