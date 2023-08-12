package ink.ptms.realms.event

import ink.ptms.realms.data.RealmBlock
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class RealmsJoinEvent(
    val player: Player,
    val realmBlock: RealmBlock?,
    val previousRealmBlock: RealmBlock?,
) : BukkitProxyEvent()