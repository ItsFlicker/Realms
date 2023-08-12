package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

object PermWitherDamage : Permission{

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "witherdamage"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.WITHER_SKELETON_SKULL) {
            name = "§f凋灵造成伤害 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8凋灵造成伤害"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.damager.type == EntityType.WITHER || e.damager.type == EntityType.WITHER_SKULL) {
            e.entity.location.getRealm()?.run {
                if (!hasPermission("witherdamage", def = false)) {
                    e.isCancelled = true
                }
            } ?: kotlin.run { e.isCancelled = true }
        }
    }
}