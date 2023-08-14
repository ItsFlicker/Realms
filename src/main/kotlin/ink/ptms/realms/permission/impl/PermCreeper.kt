package ink.ptms.realms.permission.impl

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.display
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

object PermCreeper : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "creeper"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.CREEPER_HEAD) {
            name = "§f苦力怕爆炸 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8苦力怕爆炸"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.CREEPER) {
            e.entity.location.getRealm()?.run {
                if (!hasPermission("creeper", def = false)) {
                    e.isCancelled = true
                }
            }
        }
    }
}