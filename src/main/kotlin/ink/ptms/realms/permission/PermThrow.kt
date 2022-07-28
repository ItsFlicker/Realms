package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
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

object PermThrow : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "throw"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.EGG) {
            name = "§f抛射物 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8抛射鸡蛋, 抛射雪球, 扔掷三叉戟"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        val item = e.item ?: return
        if (item.type == Material.SNOWBALL || item.type == Material.EGG || item.type == Material.TRIDENT){
            e.player.location.getRealm()?.run {
                if (!isAdmin(e.player) && !hasPermission("throw", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }
}