package ink.ptms.realms.data

import com.google.gson.JsonObject
import ink.ptms.realms.util.toAABB
import org.bukkit.Location
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.sendTo
import taboolib.common.util.Vector
import taboolib.module.effect.ParticleSpawner
import taboolib.module.effect.shape.Cube
import taboolib.module.effect.shape.Line
import taboolib.module.navigation.BoundingBox
import taboolib.platform.util.toProxyLocation
import java.awt.Color

/**
 * Realms
 * ink.ptms.realms.data.RealmBlock
 *
 * @author sky
 * @since 2021/3/11 5:09 下午
 */
class RealmBlock(center: Location, var size: Int, var name: String) {

    val center = center
        get() = field.clone()

    var tploc = this.center.add(0.0, 1.0, 0.0)
        get() = field.clone()

    val permissions = HashMap<String, Boolean>()

    val users = HashMap<String, MutableMap<String, Boolean>>()

    val extends = HashMap<Location, Int>()
    val aabb = ArrayList<BoundingBox>()

    var owner: String = name
    var joinTell: String = "§e+ §f$name | 欢迎"
    var leaveTell: String = "§e- §f$name | 慢走"

    val node: String
        get() = "realm_${center.blockX}_${center.blockY}_${center.blockZ}"

    val json: String
        get() = JsonObject().also { json ->
            json.addProperty("size", size)
            json.add("permissions", JsonObject().also { perm ->
                permissions.forEach {
                    perm.addProperty(it.key, it.value)
                }
            })
            json.add("users", JsonObject().also { user ->
                users.forEach {
                    user.add(it.key, JsonObject().also { u ->
                        it.value.forEach { (k, v) ->
                            u.addProperty(k, v)
                        }
                    })
                }
            })
            json.add("extends", JsonObject().also { ext ->
                extends.forEach {
                    ext.addProperty("${it.key.x},${it.key.y},${it.key.z}", it.value)
                }
            })
            json.addProperty("name", name)
            json.addProperty("owner", owner)
            json.addProperty("joinTell", joinTell)
            json.addProperty("leaveTell", leaveTell)
        }.toString()

    init {
        update()
    }

    /**
     * 权限检查
     */
    fun hasPermission(key: String, player: String? = null, def: Boolean = false): Boolean {
        return if (player != null && users.containsKey(player)) {
            users[player]!![key] ?: permissions[key] ?: def
        } else {
            permissions[key] ?: def
        }
    }

    /**
     * 缓存中心及扩展的碰撞箱
     */
    fun update() {
        aabb.clear()
        aabb.add(center.toCenterLocation().toAABB(size))
        aabb.addAll(extends.map { it.key.toCenterLocation().toAABB(it.value) })
    }

    /**
     * 是否在领域内
     */
    fun inside(loc: Location): Boolean {
        return aabb.any { it.contains(loc.x, loc.y, loc.z) }
    }

    /**
     * 判断是否碰撞
     * 就是各轴互相是否包含，(other 包含当前包围盒) || (当前的包围盒包含 other)
     */
    fun intersect(other: BoundingBox): Boolean {
        return aabb.any {
            return@any ((it.minX >= other.minX && it.minX <= other.maxX) || (other.minX >= it.minX && other.minX <= it.maxX))
                    && ((it.minY >= other.minY && it.minY <= other.maxY) || (other.minY >= it.minY && other.minY <= it.maxY))
                    && ((it.minZ >= other.minZ && it.minZ <= other.maxZ) || (other.minZ >= it.minZ && other.minZ <= it.maxZ))
        }
    }

    /**
     * 展示领地边界和子领域连接
     */
    fun particleDisplay() {
        aabb.forEach { box ->
            Cube(
                Location(center.world, box.minX, box.minY, box.minZ).toProxyLocation(),
                Location(center.world, box.maxX, box.maxY, box.maxZ).toProxyLocation(),
                1.0,
                object : ParticleSpawner {
                    override fun spawn(location: taboolib.common.util.Location) {
                        ProxyParticle.END_ROD.sendTo(location, range = 100.0, offset = Vector(0, 0, 0), count = 1)
                    }
            }).show()
        }
        extends.forEach { (location, _) ->
            Line(
                center.toCenterLocation().toProxyLocation(),
                location.toCenterLocation().toProxyLocation(),
                0.5,
                object : ParticleSpawner {
                    override fun spawn(location: taboolib.common.util.Location) {
                        ProxyParticle.REDSTONE.sendTo(location, 50.0, Vector(0, 0, 0), 5, data = ProxyParticle.DustData(
                            Color(152, 249, 255), 1f))
                    }
                }
            ).show()
        }
    }

    private fun BoundingBox.containsIn(x: Double, y: Double, z: Double): Boolean {
        return x - 1 > minX && x + 1 < maxX && y - 1 > minY && y + 1 < maxY && z - 1 > minZ && z + 1 < maxZ
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RealmBlock) return false
        if (node != other.node) return false
        return true
    }

    override fun hashCode(): Int {
        return node.hashCode()
    }

    override fun toString(): String {
        return "RealmBlock(size=$size, name=$name, permissions=$permissions, users=$users, extends=$extends, node='$node')"
    }
}