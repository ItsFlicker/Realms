package ink.ptms.realms.event

import ink.ptms.realms.RealmManager.getRealm
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.SubscribeEvent

object RealmsJoinLeaveListener {

    @SubscribeEvent
    fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        if (event.from.x != event.to.x || event.from.y != event.from.y || event.from.z != event.to.z) {
            val from = event.from.getRealm()
            val to = event.to.getRealm()
            if (from != to) {
                RealmsJoinEvent(event.player, to, from).call().let {
                    if (!it) event.to = event.from
                }
                RealmsLeaveEvent(event.player, from, to).call().let {
                    if (!it) event.from = event.to
                }
            }
        }
    }
}