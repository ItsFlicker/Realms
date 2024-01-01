package ink.ptms.realms.database

import ink.ptms.realms.data.RealmBlock
import org.bukkit.entity.Player
import taboolib.common.util.unsafeLazy
import taboolib.expansion.db
import taboolib.expansion.persistentContainer
import taboolib.module.configuration.ConfigNode
import java.util.concurrent.Executors

object RealmDatabase {

    @ConfigNode("database.table")
    var table = "realm_block_data"

    private val container by unsafeLazy {
        persistentContainer(db()) { new<RealmBlock>(table) }
    }

    private val pool = Executors.newFixedThreadPool(4)

    fun getAll(): List<RealmBlock> {
        return container[table].get<RealmBlock>()
    }

    fun getByServerName(serverName: String): List<RealmBlock> {
        return container[table].get<RealmBlock> {
            "server_name" eq serverName
        }
    }

    fun getByPlayer(player: Player): List<RealmBlock> {
        return container[table].get<RealmBlock> {
            "owner" eq player.uniqueId.toString()
        }
    }

    fun update(data: RealmBlock) {
        pool.submit {
            container[table].updateByKey(data)
        }
    }

    fun delete(data: RealmBlock) {
        pool.submit {
            container[table].delete<RealmBlock>(data.center) {
                "server_name" eq data.serverName
            }
        }
    }

    fun close() {
        container.close()
    }

}