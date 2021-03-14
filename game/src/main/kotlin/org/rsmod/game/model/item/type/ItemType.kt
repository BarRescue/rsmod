package org.rsmod.game.model.item.type

import org.rsmod.game.cache.type.ConfigType
import org.rsmod.game.cache.type.ConfigTypeList

data class ItemType(
    override val id: Int,
    val name: String,
    val cost: Int,
    val stacks: Boolean,
    val members: Boolean,
    val groundOptions: List<String?>,
    val inventoryOptions: List<String?>,
    val exchangeable: Boolean,
    val teamCape: Int,
    val noteLink: Int,
    val noteValue: Int, /* TODO: naming */
    val placeholderLink: Int,
    val placeholderValue: Int, /* TODO: naming */
    val parameters: Map<Int, Any>
) : ConfigType

class ItemTypeList : ConfigTypeList<ItemType>()
