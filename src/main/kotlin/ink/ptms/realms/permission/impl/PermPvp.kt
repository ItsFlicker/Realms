package ink.ptms.realms.permission.impl

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

object PermPvp : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "pvp"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.NETHERITE_SWORD) {
            name = "§fPVP ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8PVP"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.entity is Player && e.damager is Player && !e.damager.isOp) {
            e.entity.location.getRealm()?.run {
                if (!hasPermission("pvp", def = false)) {
                    e.isCancelled = true
                    (e.damager as Player).warning()
                }
            } ?: kotlin.run { e.isCancelled = true }
        }
    }
}