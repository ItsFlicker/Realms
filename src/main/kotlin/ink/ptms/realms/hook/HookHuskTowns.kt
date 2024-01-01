package ink.ptms.realms.hook

import ink.ptms.realms.RealmManager.error
import ink.ptms.realms.RealmManager.realms
import net.william278.husktowns.api.HuskTownsAPI
import net.william278.husktowns.events.ClaimEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import taboolib.common.platform.Ghost
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.unsafeLazy

object HookHuskTowns {

    private val isHooked by unsafeLazy { Bukkit.getPluginManager().getPlugin("HuskTowns") != null }

    @Ghost
    @SubscribeEvent(ignoreCancelled = true)
    fun onTownClaim(e: ClaimEvent) {
        val world = e.player.world
        val chunk = e.townClaim.claim.chunk
        val center = Location(world, chunk.x * 16.0, 64.0, chunk.z * 16.0).add(8.0, 0.0, 8.0)
        center.y = world.getHighestBlockYAt(center) + 1.0
        if (world.realms().any { it.inside(center) }) {
            e.isCancelled = true
            e.player.error("此位置与某个领域冲突!")
        }
    }

    fun hasConflict(loc: Location): Boolean {
        if (!isHooked) {
            return false
        }
        return HuskTownsAPI.getInstance().getClaimAt(loc.chunk).isPresent
    }

}