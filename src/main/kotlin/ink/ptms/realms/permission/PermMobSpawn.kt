package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.entity.Mob
import org.bukkit.entity.Monster
import org.bukkit.event.entity.CreatureSpawnEvent
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
object PermMobSpawn : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "mob_spawn"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.ZOMBIE_SPAWN_EGG) {
            name = "§f怪物生成 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8怪物自然生成"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: CreatureSpawnEvent) {
        if (e.entity is Monster && e.spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL){
            e.entity.location.getRealm()?.run {
                if (!hasPermission("mob_spawn", def = false)) {
                    e.isCancelled = true
                }
            }
        }
    }
}