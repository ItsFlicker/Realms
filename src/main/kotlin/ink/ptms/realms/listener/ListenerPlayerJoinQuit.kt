package ink.ptms.realms.listener

import ink.ptms.realms.util.Helper
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

object ListenerPlayerJoinQuit {

    private fun disconnect(player: Player) {
        Helper.cooldown.reset(player.name)
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        disconnect(e.player)
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onKick(e: PlayerKickEvent) {
        disconnect(e.player)
    }

}