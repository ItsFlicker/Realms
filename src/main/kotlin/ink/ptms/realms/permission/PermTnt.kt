package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

object PermTnt : Permission{

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "tnt"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.TNT) {
            name = "§fTNT爆炸 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8TNT爆炸"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: ExplosionPrimeEvent) {
        if (e.entityType == EntityType.PRIMED_TNT) {
            e.entity.location.getRealm()?.run {
                if (!hasPermission("tnt", def = false)) {
                    e.isCancelled = true
                }
            }
        }
    }
}