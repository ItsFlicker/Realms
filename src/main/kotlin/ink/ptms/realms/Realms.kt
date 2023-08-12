package ink.ptms.realms

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object Realms {

    @Config
    lateinit var conf: Configuration
        private set

    val realmsDust: ItemStack
        get() = conf.getItemStack("realms-dust")!!

}
