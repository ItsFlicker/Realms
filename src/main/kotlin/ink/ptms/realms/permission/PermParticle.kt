package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Realms
 * ink.ptms.realms.permission.PermAdmin
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermParticle : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "particle"

    override val priority: Int
        get() = -1

    override val default: Boolean
        get() = false

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.SEA_LANTERN) {
            name = "§f边界特效 ${value.display}"
            lore += listOf(
                "",
                "§7启用时:",
                "§8自动播放领域边界粒子"
            )
            if (value) shiny()
        }
    }
}