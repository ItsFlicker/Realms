package ink.ptms.realms

import ink.ptms.realms.database.RealmDatabase
import org.bukkit.entity.Player
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.severe
import taboolib.common5.cint
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object Realms : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    var serverName: String? = null
        private set

    val realmsDust get() = conf.getItemStack("realms-dust")!!

    override fun onLoad() {
        serverName = conf.getString("server-name")
        if (serverName == null) {
            severe("Server name has not been set!")
            disablePlugin()
        }
    }

    override fun onDisable() {
        RealmDatabase.close()
    }

    fun getPlayerMaxRealm(player: Player): Int {
        player.recalculatePermissions()
        return player.effectivePermissions.maxOf {
            val permission = it.permission
            if (permission.startsWith("realms.maxrealm.")) {
                permission.removePrefix("realms.maxrealm.").cint
            } else {
                5
            }
        }
    }

}
