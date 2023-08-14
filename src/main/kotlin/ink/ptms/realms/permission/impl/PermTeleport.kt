package ink.ptms.realms.permission.impl

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Realms
 * ink.ptms.realms.permission.impl.PermTeleport
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermTeleport : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "teleport"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.ENDER_PEARL) {
            name = "§f传送 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8通过传送进入"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerTeleportEvent) {
        e.to.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("teleport", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }
}