package ink.ptms.realms.database

import ink.ptms.realms.data.RealmBlock
import taboolib.common.util.unsafeLazy
import taboolib.expansion.dbSection
import taboolib.expansion.persistentContainer
import taboolib.module.configuration.ConfigNode
import java.util.concurrent.Executors

object RealmDatabase {

    @ConfigNode("database.table")
    var table = "realm_block_data"

    private val container by unsafeLazy {
        persistentContainer(dbSection()) { new<RealmBlock>(table) }
    }

    private val pool = Executors.newFixedThreadPool(4)

    fun getAll(): List<RealmBlock> {
        return container[table].get<RealmBlock>()
    }

    fun update(data: RealmBlock) {
        pool.submit {
            container[table].update(data)
        }
    }

    fun delete(data: RealmBlock) {
        pool.submit {
            container[table].delete<RealmBlock>(data.center)
        }
    }

    fun close() {
        container.close()
    }

}