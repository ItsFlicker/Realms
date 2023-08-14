package ink.ptms.realms.database

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.reference
import ink.ptms.realms.data.Position
import taboolib.expansion.CustomType
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.ColumnTypeSQLite

object TypeMutableMap : CustomType {

    override val type: Class<*> = MutableMap::class.java

    override val typeSQLite: ColumnTypeSQLite = ColumnTypeSQLite.TEXT

    override val typeSQL: ColumnTypeSQL = ColumnTypeSQL.VARCHAR

    override val length: Int = 2048

    override fun serialize(value: Any): Any {
        return JSON.toJSONString(value)
    }

    override fun deserialize(value: Any): Any {
        val obj = value.toString()
        return if (obj.startsWith("{{")) {
            JSON.parseObject(obj, reference<MutableMap<Position, Int>>())
        } else {
            JSON.parseObject(obj, reference<MutableMap<*, *>>())
        }
    }

}