package ink.ptms.realms.permission.impl

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import org.bukkit.entity.IronGolem
import org.bukkit.entity.Player
import org.bukkit.entity.Snowman
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Realms
 * ink.ptms.realms.permission.impl.PermDamageGolem
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermDamageGolem : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "damage_golem"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.STONE_SWORD) {
            name = "§f攻击傀儡 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8对傀儡 (IronGolem,Snowman) 造成伤害"
            )
            if (value) shiny()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.entity is IronGolem || e.entity is Snowman) {
            val player = e.damager as? Player ?: return
            e.entity.location.getRealm()?.run {
                if (!isAdmin(player) && !hasPermission("damage_golem", player.name)) {
                    e.isCancelled = true
                    player.warning()
                }
            }
        }
    }
}