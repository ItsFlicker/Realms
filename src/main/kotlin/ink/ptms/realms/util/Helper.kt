package ink.ptms.realms.util

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common5.Baffle
import taboolib.module.chat.colored
import java.util.concurrent.TimeUnit

/**
 * @Author sky
 * @Since 2020-01-05 23:19
 */
interface Helper {

    fun String.screen(): String {
        return this.replace("[^A-Za-z0-9\\u4e00-\\u9fa5_]".toRegex(), "")
    }

    fun String.process(): String {
        return this.colored()
            .replace("true", "§a开启§7")
            .replace("false", "§c关闭§7")
            .replace("null", "空")
    }

    fun List<String>.process(): List<String> {
        return this.map { it.process() }
    }

    fun CommandSender.done(message: String) {
        toDone(this, message)
    }

    fun CommandSender.info(message: String) {
        toInfo(this, message)
    }

    fun CommandSender.error(message: String) {
        toError(this, message)
    }

    fun player(name: String): Player? {
        return Bukkit.getPlayerExact(name)
    }

    fun toInfo(sender: CommandSender, message: String) {
        sender.sendMessage("§8[§c Realms §8] §7$message".colored())
        if (sender is Player && !cooldown.hasNext(sender.name)) {
            sender.playSound(sender.location, Sound.UI_BUTTON_CLICK, 1f, (1..2).random().toFloat())
        }
    }

    fun toError(sender: CommandSender, message: String) {
        sender.sendMessage("§8[§c Realms §8] §7$message".colored())
        if (sender is Player && !cooldown.hasNext(sender.name)) {
            sender.playSound(sender.location, Sound.ENTITY_VILLAGER_NO, 1f, (1..2).random().toFloat())
        }
    }

    fun toDone(sender: CommandSender, message: String) {
        sender.sendMessage("§8[§c Realms §8] §7$message".colored())
        if (sender is Player && !cooldown.hasNext(sender.name)) {
            sender.playSound(sender.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, (1..2).random().toFloat())
        }
    }

    fun toConsole(message: String) {
        console().sendMessage("§8[§c Realms §8] §7$message".colored())
    }

    companion object {

        val cooldown = Baffle.of(100, TimeUnit.MILLISECONDS)
    }
}