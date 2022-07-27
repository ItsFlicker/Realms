package ink.ptms.realms.util

import org.bukkit.World
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
    val vertex = mutableListOf<Vector>()
    Line(
        Location(null, minX, minY, maxZ),
        Location(null, maxX, maxY, minZ),
        1.0,
        object : ParticleSpawner {
            override fun spawn(location: Location) {
                vertex.add(Vector())
            }
        }
    )
    Line(
        Location(null, maxX, minY, minZ),
        Location(null, minX, maxY, maxZ),
        1.0,
        object : ParticleSpawner {
            override fun spawn(location: Location) {
                vertex.add(Vector())
            }
        }
    )
    return vertex.toList()
//    return listOf(
//        Vector(minX, minY, minZ),
//        Vector(maxX, minY, minZ),
//        Vector(maxX, minY, maxZ),
//        Vector(minX, minY, maxZ),
//        Vector(minX, maxY, minZ),
//        Vector(maxX, maxY, minZ),
//        Vector(maxX, maxY, maxZ),
//        Vector(minX, maxY, maxZ),
//        Vector(minX, (maxY+minY)/2, minZ),
//        Vector(maxX, (maxY+minY)/2, minZ),
//        Vector(maxX, (maxY+minY)/2, maxZ),
//        Vector(minX, (maxY+minY)/2, maxZ),
//        Vector((maxX+minX)/2, minY, (maxZ+minZ)/2),
//        Vector((maxX+minX)/2, maxY, (maxZ+minZ)/2),
//        Vector((maxX+minX)/2, (maxY+minY)/2, (maxZ+minZ)/2)
//    )
}

fun BoundingBox.getBlocksWithinBB(world: World): MutableList<Location> {
    val blocklist = mutableListOf<Location>()
    for (x in minX.toInt()..maxX.toInt()) {
        for (y in minY.toInt()..maxY.toInt()) {
            for (z in minZ.toInt()..maxZ.toInt()) {
                blocklist.add(Location(world.name, x.toDouble(), y.toDouble(), z.toDouble()))
            }
        }
    }
    return blocklist
}

val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

fun Player.warning() {
    adaptPlayer(this).sendActionBar("§c§l:(§7 当前行为受所属领域保护.")
}