package ink.ptms.realms

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.getRealmSize
import ink.ptms.realms.RealmManager.setRealmSize
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.platform.util.isAir

/**
 * Realms
 * ink.ptms.realms.RealmCommand
 *
 * @author sky
 * @since 2021/3/11 10:54 下午
 */
@CommandHeader("realm", permissionDefault = PermissionDefault.TRUE)
object RealmCommand {

    @CommandBody(permission = "admin")
    val setRealmSize = subCommand {
        dynamic {
            restrict<Player> { _, _, argument ->
                Coerce.asInteger(argument).isPresent
            }
            execute<Player> { sender, _, argument ->
                if (sender.inventory.itemInMainHand.isAir()) {
                    sender.sendMessage("你无法给空气设置领域大小。")
                    return@execute
                }
                sender.inventory.itemInMainHand.setRealmSize(Coerce.toInteger(argument))
                sender.sendMessage("当前手中物品的领域大小为${sender.inventory.itemInMainHand.getRealmSize()}格。")
            }
        }
    }

    @CommandBody
    val show = subCommand {
        execute<Player> { sender, _, _ ->
            sender.location.getRealm()?.run {
                submit(async = true) {
                    borderDisplay()
                }
            }
        }
    }
}