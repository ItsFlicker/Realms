package ink.ptms.realms.util

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