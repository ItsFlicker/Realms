package ink.ptms.realms.permission.impl

import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.display
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Realms
 * ink.ptms.realms.permission.impl.PermAdmin
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermAdmin : Permission {

    @Awake(LifeCycle.INIT)
    internal fun init() {
        register()
    }

    override val id: String
        get() = "admin"

    override val priority: Int
        get() = -1

    override val worldSide: Boolean
        get() = false

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.COMMAND_BLOCK) {
            name = "§f最高权力 ${value.display}"
            lore += listOf(
                "",
                "§7允许行为:",
                "§8破坏领域, 扩展领域, 管理领域",
                "",
                "§4注意!",
                "§c对方将获得你的所有权力"
            )
            if (value) shiny()
            colored()
        }
    }
}