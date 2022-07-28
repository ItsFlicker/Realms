package ink.ptms.realms.util

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.module.effect.Line
import taboolib.module.effect.ParticleSpawner
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
    val vertex = mutableSetOf<Vector>()
    Line(
        Location(null, minX, minY, maxZ),
        Location(null, maxX, maxY, minZ),
        1.0,
        object : ParticleSpawner {
            override fun spawn(location: Location) {
                vertex.add(Vector(location.x, location.y, location.z))
            }
        }
    ).show()
    Line(
        Location(null, maxX, minY, minZ),
        Location(null, minX, maxY, maxZ),
        1.0,
        object : ParticleSpawner {
            override fun spawn(location: Location) {
                vertex.add(Vector(location.x, location.y, location.z))
            }
        }
    ).show()
    return vertex.toList()
}

val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

fun Player.warning() {
    adaptPlayer(this).sendActionBar("§c§l:(§7 当前行为受所属领域保护.")
}