package ink.ptms.realms

import ink.ptms.realms.RealmManager.done
import ink.ptms.realms.RealmManager.error
import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.getRealmSize
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.save
import ink.ptms.realms.RealmManager.setRealmSize
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.command.*
import taboolib.common.platform.function.submitAsync
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.platform.util.isAir

/**
 * Realms
 * ink.ptms.realms.RealmCommand
 *
 * @author sky
 * @since 2021/3/11 10:54 下午
 */
@CommandHeader("realm", ["res"], permissionDefault = PermissionDefault.TRUE)
object RealmCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permission = "realms.admin", optional = true)
    val setrealmsize = subCommand {
        dynamic {
            restrict<Player> { _, _, argument ->
                Coerce.asInteger(argument).isPresent
            }
            execute<Player> { sender, _, argument ->
                if (sender.inventory.itemInMainHand.isAir()) {
                    return@execute sender.sendMessage("你无法给空气设置领域大小。")
                }
                sender.inventory.itemInMainHand.setRealmSize(Coerce.toInteger(argument))
                sender.sendMessage("当前手中物品的领域大小为${sender.inventory.itemInMainHand.getRealmSize()}格。")
            }
        }
    }

    @CommandBody(optional = true)
    val show = subCommand {
        execute<Player> { sender, _, _ ->
            submitAsync {
                sender.location.getRealm()?.particleDisplay()
            }
        }
    }

    @CommandBody(optional = true)
    val tp = subCommand {
        dynamic("realm") {
            suggestion<Player>(uncheck = true) { sender, _ ->
                RealmManager.realms.filter { sender.uniqueId == it.owner }.map { it.name }
            }
            execute<Player> { sender, ctx, _ ->
                val realm = RealmManager.realms.firstOrNull { it.name == ctx["realm"] }
                    ?: return@execute sender.error("未知的领域")
                if (realm.isAdmin(sender) || realm.hasPermission("teleport", sender.name)) {
                    sender.teleport(realm.teleportLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    sender.done("传送成功")
                } else {
                    sender.error("你没有该领域的传送权限")
                }
            }
        }
    }

    @CommandBody(optional = true)
    val tpset = subCommand {
        execute<Player> { sender, _, _ ->
            val realm = sender.location.getRealm() ?: return@execute sender.error("无效的领域")
            if (realm.isAdmin(sender)) {
                realm.teleportLocation = sender.location
                realm.save()
                sender.done("传送点已设置为你的位置")
            } else {
                sender.error("你没有该领域的管理权限")
            }
        }
    }
}