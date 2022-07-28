package ink.ptms.realms.util

import ink.ptms.realms.RealmManager
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

object PapiHook : PlaceholderExpansion {

    override val identifier: String
        get() = "realms"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        player ?: return "ERROR"
        val params = args.split("_")
        return when (params[0]) {
            "where" -> RealmManager.getRealmBlock(player.location)?.name ?: params.getOrElse(1) { "野外" }
            else -> "null"
        }
    }
}