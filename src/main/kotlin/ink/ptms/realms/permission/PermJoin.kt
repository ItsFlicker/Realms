package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.event.RealmsJoinEvent
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
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
object PermJoin : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "join"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.DIAMOND_BOOTS) {
            name = "§f进入 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8离开领域"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: RealmsJoinEvent) {
        e.player.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("join", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }
}