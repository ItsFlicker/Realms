package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.Material
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

object PermFire : Permission{

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "fire"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.BLAZE_POWDER) {
            name = "§f火焰 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8火焰蔓延, 火焰烧毁方块"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: BlockSpreadEvent) {
        if (e.source.type == Material.FIRE || e.source.type == Material.SOUL_FIRE) {
            e.block.location.getRealm()?.run {
                if (!hasPermission("fire", def = false)) {
                    e.isCancelled = true
                }
            } ?: kotlin.run { e.isCancelled = true }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: BlockBurnEvent) {
        e.block.location.getRealm()?.run {
            if (!hasPermission("fire", def = false)) {
                e.isCancelled = true
            }
        } ?: kotlin.run { e.isCancelled = true }
    }
}