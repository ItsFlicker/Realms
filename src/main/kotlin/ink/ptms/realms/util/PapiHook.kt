package ink.ptms.realms.util

import ink.ptms.realms.RealmManager
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

object PapiHook : PlaceholderExpansion {

    override val identifier: String
        get() = "realms"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        player ?: return "ERROR"
        return when (args) {
            "where" -> RealmManager.getRealmBlock(player.location)?.name ?: "野外"
            else -> "null"
        }
    }
}