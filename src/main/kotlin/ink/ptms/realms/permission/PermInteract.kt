package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
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
@PlatformSide([Platform.BUKKIT])
object PermInteract : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "interact"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.OAK_DOOR) {
            name = "§f交互 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8方块交互, 实体交互",
                "§8粘液科技机器交互"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK) {
            e.clickedBlock?.location?.getRealm()?.run {
                if (!isAdmin(e.player) && !hasPermission("interact", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerInteractEntityEvent) {
        e.rightClicked.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("interact", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }
}