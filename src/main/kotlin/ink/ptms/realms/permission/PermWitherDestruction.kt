package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

object PermWitherDestruction : Permission{

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "witherdestruction"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.WITHER_SKELETON_SKULL) {
            name = "§f凋灵破坏方块 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8凋灵自爆破坏方块, 凋灵发射的头颅爆炸"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.WITHER || e.entityType == EntityType.WITHER_SKULL) {
            e.entity.location.getRealm()?.run {
                if (!hasPermission("witherdestruction", def = false)) {
                    e.isCancelled = true
                }
            } ?: kotlin.run { e.isCancelled = true }
        }
    }
}