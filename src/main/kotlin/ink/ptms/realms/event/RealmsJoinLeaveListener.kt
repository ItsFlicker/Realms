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
                if (!RealmsJoinEvent(event.player, to, from).call()) {
                    event.to = event.from
                }
                if (!RealmsLeaveEvent(event.player, from, to).call()) {
                    event.from = event.to
                }
            }
        }
    }
}