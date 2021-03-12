package org.rsmod.plugins.api.protocol.packet.client

import com.google.inject.Inject
import org.rsmod.game.action.ActionBus
import org.rsmod.game.message.ClientPacket
import org.rsmod.game.message.ClientPacketHandler
import org.rsmod.game.model.client.Client
import org.rsmod.game.model.map.Coordinates
import org.rsmod.game.model.mob.Player
import org.rsmod.game.model.obj.GameObjectApSet
import org.rsmod.game.model.obj.GameObjectMap
import org.rsmod.game.model.obj.type.ObjectType
import org.rsmod.game.model.obj.type.ObjectTypeList
import org.rsmod.game.model.vars.type.VarbitTypeList
import org.rsmod.plugins.api.protocol.packet.ObjectAction
import org.rsmod.plugins.api.protocol.packet.ObjectClick
import org.rsmod.plugins.api.util.extractBitValue

data class OpLoc1(
    val id: Int,
    val x: Int,
    val y: Int,
    val mode: Int
) : ClientPacket

class OpLoc1Handler @Inject constructor(
    private val actionBus: ActionBus,
    private val objMap: GameObjectMap,
    private val objApSet: GameObjectApSet,
    private val objTypes: ObjectTypeList,
    private val varbitTypes: VarbitTypeList
) : ClientPacketHandler<OpLoc1> {

    override fun handle(client: Client, player: Player, packet: OpLoc1) {
        val (id, x, y) = packet
        val coords = Coordinates(x, y, player.coords.level)
        val objects = objMap[coords].filter { it.id == id }
        val obj = objects.firstOrNull()
        if (obj == null) {
            player.warn { "Operate object error: does not exist (id=$id, coords=$coords)" }
            return
        }
        val shape = obj.shape
        val rot = obj.rotation
        val type = obj.type.varType(player, objTypes, varbitTypes)
        val approach = objApSet.contains(type.id)
        val option = ObjectAction.Option1(player, type, shape, rot, coords)
        val action = ObjectClick(player, type, shape, rot, coords, option, approach)
        actionBus.publish(action)
    }
}

private fun ObjectType.varType(
    player: Player,
    objs: ObjectTypeList,
    varbits: VarbitTypeList
): ObjectType {
    if (transforms.isEmpty()) return this
    val transformIndex = when {
        varp > 0 -> player.varpMap[varp] ?: 0
        varbit > 0 -> {
            val type = varbits[varbit]
            (player.varpMap[type.varp] ?: 0).extractBitValue(type.lsb, type.msb)
        }
        else -> -1
    }
    if (transformIndex in transforms.indices) {
        val transform = transforms[transformIndex]
        return objs[transform]
    }
    return this
}
