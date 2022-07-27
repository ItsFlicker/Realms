package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.attacker
import taboolib.platform.util.buildItem

/**
 * Realms
 * ink.ptms.realms.permission.PermAdmin
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
@PlatformSide([Platform.BUKKIT])
object PermBuild : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "build"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.GRASS_BLOCK) {
            name = "§f建筑 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8放置方块, 破坏方块, 放置挂饰, 破坏挂饰",
                "§8放置盔甲架, 破坏盔甲架, 装满桶, 倒空桶"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: BlockBreakEvent) {
        e.block.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: BlockPlaceEvent) {
        e.block.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: HangingPlaceEvent) {
        val player = e.player ?: return
        e.block.location.getRealm()?.run {
            if (!isAdmin(player) && !hasPermission("build", player.name)) {
                e.isCancelled = true
                player.warning()
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: HangingBreakByEntityEvent) {
        if (e.remover is Player) {
            val player = e.remover as Player
            e.entity.location.block.location.getRealm()?.run {
                if (!isAdmin(player) && !hasPermission("build", player.name)) {
                    e.isCancelled = true
                    player.warning()
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.item?.type == org.bukkit.Material.ARMOR_STAND) {
            e.clickedBlock?.location?.getRealm()?.run {
                if (!isAdmin(e.player) && !hasPermission("build", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.entity is ArmorStand) {
            val player = e.attacker as? Player ?: return
            e.entity.location.block.location.getRealm()?.run {
                if (!isAdmin(player) && !hasPermission("build", player.name)) {
                    e.isCancelled = true
                    player.warning()
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerBucketFillEvent) {
        e.block.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerBucketEmptyEvent) {
        e.block.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }
}