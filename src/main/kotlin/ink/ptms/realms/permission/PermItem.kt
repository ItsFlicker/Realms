package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Realms
 * ink.ptms.realms.permission.PermInteract
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */

object PermItem : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "item"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.APPLE) {
            name = "§f物品 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8物品丢弃, 物品捡起"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerDropItemEvent) {
        e.player.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("item", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityPickupItemEvent) {
        if (e.entity is Player) {
            e.entity.location.getRealm()?.run {
                if (!isAdmin(e.entity as Player) && !hasPermission("item", e.entity.name)) {
                    e.isCancelled = true
                    (e.entity as Player).warning()
                }
            }
        }
    }
}