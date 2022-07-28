package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.entity.Animals
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Realms
 *
 * @author 枫溪
 * @since 2021/4/18 8:30 上午
 */

object PermAnimalsSpawn : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "animals_spawn"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.SHEEP_SPAWN_EGG) {
            name = "§f动物产生 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8生成动物"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntitySpawnEvent) {
        if (e.entity !is Animals){
            return
        }
        e.entity.location.getRealm()?.run {
            if (!hasPermission("animals_spawn", def = false)) {
                e.isCancelled = true
            }
        }
    }
}