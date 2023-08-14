package ink.ptms.realms.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Vector
import taboolib.module.navigation.BoundingBox

fun Location.toAABB(size: Int) = BoundingBox(
    x - size - 0.5,
    y - size - 0.5,
    z - size - 0.5,
    x + size + 1.5,
    y + size + 1.5,
    z + size + 1.5
)

fun toLocation(source: String): Location {
    return source.replace("__", ".").split(",").run {
        Location(
            Bukkit.getWorld(get(0)),
            getOrElse(1) { "0" }.toDouble(),
            getOrElse(2) { "0" }.toDouble(),
            getOrElse(3) { "0" }.toDouble()
        )
    }
}

fun fromLocation(location: Location): String {
    return "${location.world?.name},${location.x},${location.y},${location.z}".replace(".", "__")
}

fun BoundingBox.getVertex(): List<Vector> {
    return listOf(
        Vector(minX, minY, minZ),
        Vector(maxY, minY, minZ),
        Vector(maxY, minY, maxY),
        Vector(minX, minY, maxZ),
        Vector(minX, maxY, minZ),
        Vector(maxY, maxY, minZ),
        Vector(maxY, maxY, maxY),
        Vector(minX, maxY, maxZ),
    )
}

val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

fun Player.warning() {
    adaptPlayer(this).sendActionBar("§c§l:(§7 当前行为受所属领域保护.")
}