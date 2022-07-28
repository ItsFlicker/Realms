package ink.ptms.realms.event

import ink.ptms.realms.data.RealmBlock
import org.bukkit.entity.Player
import taboolib.common.platform.event.ProxyEvent

class RealmsLeaveEvent(
    val player: Player,
    val realmBlock: RealmBlock?,
    val previousRealmBlock: RealmBlock?,
) : ProxyEvent()