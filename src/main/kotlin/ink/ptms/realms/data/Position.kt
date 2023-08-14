package ink.ptms.realms.data

import org.bukkit.Bukkit

data class Position(
    var world: String,
    var x: Double,
    var y: Double,
    var z: Double
) {

    fun toCenter(): Position {
        return Position(world, x + 0.5, y + 0.5, z + 0.5)
    }

    fun toProxyLocation(): taboolib.common.util.Location {
        return taboolib.common.util.Location(world, x, y, z)
    }

    fun toBukkitLocation(): org.bukkit.Location {
        return org.bukkit.Location(Bukkit.getWorld(world), x, y, z)
    }

    companion object {

        fun org.bukkit.Location.toPosition(): Position {
            return Position(world.name, x, y, z)
        }
    }
}