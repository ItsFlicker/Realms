package ink.ptms.realms.database

import ink.ptms.realms.util.fromLocation
import ink.ptms.realms.util.toLocation
import org.bukkit.Location
import taboolib.expansion.CustomType
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.ColumnTypeSQLite

object TypeLocation : CustomType {

    override val type: Class<*> = Location::class.java

    override val typeSQLite: ColumnTypeSQLite = ColumnTypeSQLite.TEXT

    override val typeSQL: ColumnTypeSQL = ColumnTypeSQL.VARCHAR

    override val length = 64

    override fun serialize(value: Any): Any {
        return fromLocation(value as Location)
    }

    override fun deserialize(value: Any): Any {
        return toLocation(value.toString())
    }

}