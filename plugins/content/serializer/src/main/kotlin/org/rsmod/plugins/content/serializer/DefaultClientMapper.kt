package org.rsmod.plugins.content.serializer

import com.google.inject.Inject
import org.rsmod.game.GameEnv
import org.rsmod.game.config.GameConfig
import org.rsmod.game.model.client.Client
import org.rsmod.game.model.client.PlayerEntity
import org.rsmod.game.model.domain.PlayerId
import org.rsmod.game.model.domain.serializer.ClientData
import org.rsmod.game.model.domain.serializer.ClientDataMapper
import org.rsmod.game.model.domain.serializer.ClientDeserializeRequest
import org.rsmod.game.model.domain.serializer.ClientDeserializeResponse
import org.rsmod.game.model.map.Coordinates
import org.rsmod.game.model.mob.Player
import org.rsmod.game.model.move.MovementSpeed
import org.rsmod.game.model.stat.Stat
import org.rsmod.game.model.stat.StatKey
import org.rsmod.game.model.stat.StatMap
import org.rsmod.game.model.vars.VarpMap
import org.rsmod.util.security.PasswordEncryption

class DefaultClientMapper @Inject constructor(
    private val config: GameConfig,
    private val encryption: PasswordEncryption
) : ClientDataMapper<DefaultClientData> {

    override val type = DefaultClientData::class

    /**
     * Check if client passwords should not be verified on [deserialize].
     */
    private fun skipPasswordCheck(): Boolean {
        return config.env == GameEnv.Development
    }

    override fun deserialize(request: ClientDeserializeRequest, data: DefaultClientData): ClientDeserializeResponse {
        val password = request.plaintTextPass
        if (password == null) {
            val reconnectXteas = request.reconnectXteas
            if (reconnectXteas == null || !reconnectXteas.contentEquals(data.loginXteas)) {
                return ClientDeserializeResponse.BadCredentials
            }
        } else if (!skipPasswordCheck() && !encryption.verify(password, data.encryptedPass)) {
            return ClientDeserializeResponse.BadCredentials
        }
        val entity = PlayerEntity(
            username = data.displayName,
            rank = data.rank
        )
        val player = Player(
            id = PlayerId(data.loginName),
            loginName = data.loginName,
            eventBus = request.eventBus,
            actionBus = request.actionBus,
            entity = entity,
            messageListeners = listOf(request.messageListener)
        )
        val client = Client(
            player = player,
            device = request.device,
            machine = request.machine,
            settings = request.settings,
            encryptedPass = data.encryptedPass,
            loginXteas = request.loginXteas,
            bufAllocator = request.bufAllocator
        )
        entity.coords = when (data.coords.size) {
            2 -> Coordinates(data.coords[0], data.coords[1])
            3 -> Coordinates(data.coords[0], data.coords[1], data.coords[2])
            else -> error("Invalid coordinate values: ${data.coords}.")
        }
        player.speed = if (data.moveSpeed == 1) MovementSpeed.Run else MovementSpeed.Walk
        player.runEnergy = data.runEnergy
        player.stats.putAll(data.skills)
        player.varpMap.putAll(data.varps)
        return ClientDeserializeResponse.Success(client)
    }

    override fun serialize(client: Client): DefaultClientData {
        val player = client.player
        val entity = player.entity
        return DefaultClientData(
            loginName = player.loginName,
            displayName = player.username,
            encryptedPass = client.encryptedPass,
            loginXteas = client.loginXteas,
            coords = intArrayOf(entity.coords.x, entity.coords.y, entity.coords.level),
            rank = entity.rank,
            moveSpeed = if (player.speed == MovementSpeed.Run) 1 else 0,
            runEnergy = player.runEnergy,
            skills = player.stats.toIntKeyMap(),
            varps = player.varpMap.toMap()
        )
    }

    override fun newClient(request: ClientDeserializeRequest): Client {
        val password = request.plaintTextPass ?: error("New client must have an input password.")
        val entity = PlayerEntity(
            username = request.loginName,
            rank = 0
        )
        val player = Player(
            id = PlayerId(request.loginName),
            loginName = request.loginName,
            eventBus = request.eventBus,
            actionBus = request.actionBus,
            entity = entity,
            messageListeners = listOf(request.messageListener)
        )
        val encryptedPass = encryption.encrypt(password)
        entity.coords = config.home
        return Client(
            player = player,
            device = request.device,
            machine = request.machine,
            settings = request.settings,
            encryptedPass = encryptedPass,
            loginXteas = request.loginXteas,
            bufAllocator = request.bufAllocator
        )
    }
}

data class DefaultClientData(
    val loginName: String,
    val displayName: String,
    val encryptedPass: String,
    val loginXteas: IntArray,
    val coords: IntArray,
    val rank: Int,
    val runEnergy: Double,
    val moveSpeed: Int,
    val skills: Map<Int, Stat>,
    val varps: Map<Int, Int>
) : ClientData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultClientData

        if (loginName != other.loginName) return false
        if (displayName != other.displayName) return false
        if (encryptedPass != other.encryptedPass) return false
        if (!loginXteas.contentEquals(other.loginXteas)) return false
        if (!coords.contentEquals(other.coords)) return false
        if (rank != other.rank) return false
        if (runEnergy != other.runEnergy) return false
        if (moveSpeed != other.moveSpeed) return false
        if (skills != other.skills) return false
        if (varps != other.varps) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loginName.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + encryptedPass.hashCode()
        result = 31 * result + loginXteas.contentHashCode()
        result = 31 * result + coords.contentHashCode()
        result = 31 * result + rank
        result = 31 * result + runEnergy.hashCode()
        result = 31 * result + moveSpeed
        result = 31 * result + skills.hashCode()
        result = 31 * result + varps.hashCode()
        return result
    }
}

private fun StatMap.toIntKeyMap(): Map<Int, Stat> {
    return entries.associate { (key, value) -> key.id to value }
}

private fun StatMap.putAll(intKeyMap: Map<Int, Stat>) {
    val map = intKeyMap.entries.associate { (key, value) -> StatKey(key) to value }
    putAll(map)
}

private fun VarpMap.putAll(from: Map<Int, Int>) {
    from.forEach { (key, value) -> this[key] = value }
}
