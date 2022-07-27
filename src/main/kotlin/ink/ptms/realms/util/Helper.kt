package ink.ptms.realms.util

import ink.ptms.realms.Realms
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common5.Baffle
import taboolib.common5.Coerce
import taboolib.module.chat.colored
import taboolib.module.nms.getI18nName
import taboolib.platform.compat.replacePlaceholder
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @Author sky
 * @Since 2020-01-05 23:19
 */
interface Helper {

    fun String.toPlayer(): Player? {
        return player(this)
    }

    fun Entity.getCName(): String {
        if (this.customName != null) {
            return this.customName!!
        }
        return this.getI18nName()
    }

    fun UUID.toPlayer(): Player? {
        return Bukkit.getPlayer(this)
    }

    fun String.toPapi(player: Player): String {
        return this.replacePlaceholder(player)
    }

    fun List<String>.toPapi(player: Player): List<String> {
        return this.replacePlaceholder(player)
    }

    fun Double.toTwo(): Double {
        return Coerce.format(this)
    }

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

    fun heal(player: Player) {
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.fireTicks = 0
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
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

    fun run(runnable: () -> (Unit)) {
        Bukkit.getScheduler().runTask(Realms.plugin, Runnable { runnable.invoke() })
    }

    fun runAsync(runnable: () -> (Unit)) {
        Bukkit.getScheduler().runTaskAsynchronously(Realms.plugin, Runnable { runnable.invoke() })
    }

    companion object {

        val cooldown = Baffle.of(100, TimeUnit.MILLISECONDS)
    }
}