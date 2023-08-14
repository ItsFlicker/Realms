package ink.ptms.realms

import ink.ptms.realms.database.RealmDatabase
import taboolib.common.platform.Plugin
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object Realms : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    val realmsDust get() = conf.getItemStack("realms-dust")!!

    override fun onDisable() {
        RealmDatabase.close()
    }

}
