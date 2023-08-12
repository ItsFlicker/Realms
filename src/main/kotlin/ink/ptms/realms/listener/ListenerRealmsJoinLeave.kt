package ink.ptms.realms.listener

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.event.RealmsJoinEvent
import ink.ptms.realms.event.RealmsLeaveEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.SubscribeEvent

object ListenerRealmsJoinLeave {

    @SubscribeEvent(ignoreCancelled = true)
    fun onPlayerMoveEvent(e: PlayerMoveEvent) {
        if (e.from.x != e.to.x || e.from.y != e.to.y || e.from.z != e.to.z) {
            val from = e.from.getRealm()
            val to = e.to.getRealm()
            if (from != to) {
                if (!RealmsJoinEvent(e.player, to, from).call()) {
                    e.to = e.from
                }
                if (!RealmsLeaveEvent(e.player, from, to).call()) {
                    e.from = e.to
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onTeleport(e: PlayerTeleportEvent) {
        val from = e.from.getRealm()
        val to = e.to.getRealm()
        if (from != to) {
            if (!RealmsLeaveEvent(e.player, from, to).call()) {
                e.isCancelled = true
            }
            if (!RealmsJoinEvent(e.player, to, from).call()) {
                e.isCancelled = true
            }
        }
    }

}