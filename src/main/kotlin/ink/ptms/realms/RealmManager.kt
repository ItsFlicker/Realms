package ink.ptms.realms

import com.google.gson.JsonParser
import ink.ptms.blockdb.BlockFactory.createDataContainer
import ink.ptms.blockdb.Data
import ink.ptms.blockdb.event.BlockDataDeleteEvent
import ink.ptms.realms.data.RealmBlock
import ink.ptms.realms.data.RealmWorld
import ink.ptms.realms.event.RealmsJoinEvent
import ink.ptms.realms.event.RealmsLeaveEvent
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.Helper
import ink.ptms.realms.util.getVertex
import ink.ptms.realms.util.toAABB
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Player
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common.platform.sendTo
import taboolib.common.util.Vector
import taboolib.common5.Baffle
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.module.effect.Line
import taboolib.module.effect.ParticleSpawner
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.util.*
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

/**
 * Realms
 * ink.ptms.realms.RealmManager
 *
 * @author sky
 * @since 2021/3/11 5:09 下午
 */
object RealmManager : Helper {

    private val permissions = ArrayList<Permission>()
    private val worlds = ConcurrentHashMap<String, RealmWorld>()
    private val baffle = Baffle.of(10)

    @Awake(LifeCycle.ENABLE)
    internal fun init() {
        Bukkit.getWorlds().forEach {
            val realmWorld = it.realmWorld()
            it.loadedChunks.forEach { chunk ->
                realmWorld.realms[chunk.chunkKey] = chunk.realms().toMutableList()
            }
        }
    }

    @Schedule(async = true, period = 40)
    fun particle() {
        Bukkit.getWorlds().forEach {
            it.realms().forEach { realm ->
                ProxyParticle.DOLPHIN.sendTo(realm.center.toCenterLocation().toProxyLocation(), 25.0, Vector(0.5, 0.5, 0.5), 5)

                realm.extends.forEach { (location, _) ->
                    ProxyParticle.REDSTONE.sendTo(location.toProxyLocation(), 25.0, Vector(0.5, 0.5, 0.5), 5, data = ProxyParticle.DustData(Color(152, 249, 255), 1f))
                    Line(
                        realm.center.toCenterLocation().toProxyLocation(),
                        location.toCenterLocation().toProxyLocation(),
                        0.35,
                        object : ParticleSpawner {
                            override fun spawn(location: taboolib.common.util.Location) {
                                ProxyParticle.REDSTONE.sendTo(location, 25.0, Vector(0, 0, 0), 5, data = ProxyParticle.DustData(Color(152, 249, 255), 1f))
                            }
                        }
                    ).show()
                }
                if (realm.hasPermission("particle", def = false) && baffle.hasNext()) {
                    realm.borderDisplay()
                }
            }
        }

    }

    @SubscribeEvent
    internal fun e(e: ChunkLoadEvent) {
        val realmWorld = e.chunk.world.realmWorld()
        if (realmWorld.realms.containsKey(e.chunk.chunkKey)) {
            return
        }
        realmWorld.realms[e.chunk.chunkKey] = e.chunk.realms().toMutableList()
    }

    @SubscribeEvent
    internal fun e(e: ChunkUnloadEvent) {
        val realmWorld = e.chunk.world.realmWorld()
        if (realmWorld.realms.containsKey(e.chunk.chunkKey)) {
            realmWorld.realms.remove(e.chunk.chunkKey)
        }
    }

    @SubscribeEvent
    internal fun e(e: BlockDataDeleteEvent) {
        if (e.reason != BlockDataDeleteEvent.Reason.BREAK) {
            e.isCancelled = true
        }
    }

    /**
     * 活塞推出方块
     */
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    internal fun e(e: BlockPistonExtendEvent) {
        e.check(e.blocks)
    }

    /**
     * 胡塞收回方块
     */
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    internal fun e(e: BlockPistonRetractEvent) {
        e.check(e.blocks)
    }

    private fun BlockPistonEvent.check(blocks: List<Block>) {
        for (block in blocks) {
            if (block.isRealmBlock()) {
                isCancelled = true
                return
            }
        }
    }

    @SubscribeEvent
    internal fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock!!.isRealmBlock()) {
            e.isCancelled = true
            val realmBlock = e.clickedBlock!!.getRealmBlock()!!
            if (realmBlock.hasPermission("admin", e.player.name)) {
                realmBlock.open(e.player)
            } else {
//                e.player.sendHolographic(e.clickedBlock!!.location.add(0.5, 1.0, 0.5), "§c:(", "§7这不属于你.")
                e.player.info("§c:( §7这不属于你.")
            }
        }
    }

    @SubscribeEvent
    internal fun e(e: BlockBreakEvent) {
        if (e.block.isRealmBlock()) {
            e.isCancelled = true
            val realmBlock = e.block.getRealmBlock()!!
            if (realmBlock.hasPermission("admin", e.player.name) || e.player.isOp) {
                // 破坏核心
                if (realmBlock.center == e.block.location) {
                    // 存在扩展
                    if (realmBlock.extends.isNotEmpty()) {
//                        e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "§c:(", "§7需要先移除所有子领域.")
                        e.player.info("§c:( §7需要先移除所有子领域.")
                    } else {
//                        e.player.sendHolographic(e.block.location.toCenterLocation(), "§c:(", "§7领域已移除.")
                        e.player.info("§c:( §7领域已移除.")
                        e.block.world.playEffect(e.block.location.toCenterLocation(), Effect.STEP_SOUND, e.block.type)
                        e.block.type = Material.AIR
                        e.block.world.dropItem(e.block.location.toCenterLocation(), Realms.realmsDust.also {
                            it.amount = realmBlock.size
                        })
                        realmBlock.remove()
                    }
                }
                // 破坏扩展
                else if (realmBlock.extends.containsKey(e.block.location)) {
                    e.block.world.playEffect(e.block.location.toCenterLocation(), Effect.STEP_SOUND, e.block.type)
                    e.block.type = Material.AIR
                    e.block.world.dropItem(e.block.location.toCenterLocation(), Realms.realmsDust.also {
                        it.amount = realmBlock.extends.remove(e.block.location)!!
                    })
//                    e.player.sendHolographic(e.block.location.toCenterLocation(), "§c:(", "§7领域已移除.")
                    e.player.info("§c:( §7领域已移除.")
                    realmBlock.update()
                    realmBlock.save()
                }
            } else {
//                e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "§c:(", "§7这不属于你.")
                e.player.info("§c:( §7这不属于你.")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    internal fun e(e: BlockPlaceEvent) {
        val realmSize = e.itemInHand.getRealmSize()
        if (realmSize > 0) {
            val item = e.itemInHand.clone()
            e.player.info("领域计算中, 请稍等")
            submit(async = true) {
                val vertex = e.block.location.toCenterLocation().toProxyLocation().toAABB(realmSize).getVertex().mapNotNull { vertex ->
                    vertex.toLocation(e.block.world.name).toBukkitLocation().getRealm()
                }
                submit {
                    when {
                        vertex.isEmpty() -> {
                            e.player.inputSign(arrayOf("", "", "↑请输入领域名称")) { les ->
                                val names = "${les[0]}${les[1]}".screen()
                                if (names.isEmpty()) {
                                    e.player.error("已取消创建领地!")
                                    e.block.type = Material.AIR
                                    e.player.inventory.addItem(item)
                                    return@inputSign
                                }
                                RealmBlock(e.block.location, realmSize).also { it.name = names }.create(e.player)
                                e.block.createDataContainer()["realms"] = Data(true)
                                e.block.blockData = e.block.blockData.also {
                                    if (it is Waterlogged) {
                                        it.isWaterlogged = true
                                    }
                                }
                                e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
//                        e.player.sendHolographic(e.block.location.toCenterLocation().add(0.0, 1.0, 0.0), "§e:)", "§f领域已创建")
                                e.player.info("§e:) §f领域已创建")
                            }
                        }
                        vertex.any { !it.hasPermission("admin", e.player.name) } -> {
//                    e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7当前位置与其他领域冲突.")
                            e.player.info("§4:( §7当前位置与其他领域冲突.")
                            e.block.type = Material.AIR
                            e.player.inventory.addItem(item)
                        }
                        else -> {
                            e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                            // 领地合并界面
                            e.player.openMenu<Linked<RealmBlock>>("领域合并") {
                                rows(6)
                                elements {
                                    vertex.toSet().toList()
                                }
                                slots(inventoryCenterSlots)
                                setPreviousPage(47) { _, hasPreviousPage ->
                                    if (hasPreviousPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f上一页" }
                                    else buildItem(XMaterial.ARROW) { name = "§8上一页"}
                                }
                                setNextPage(51) { _, hasNextPage ->
                                    if (hasNextPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f下一页" }
                                    else buildItem(XMaterial.ARROW) { name = "§8下一页"}
                                }
                                set(49, buildItem(XMaterial.OAK_SIGN) {
                                    name = "§f领域合并"
                                    lore += listOf("§7当新领域与多个属于你的领域重合时",
                                        "§7将会作为子领域为已选择的领域扩展",
                                        "",
                                        "§4注意!",
                                        "§c其他未选择的领域同时降级为子领域",
                                        "§c多个连续重合的领域只有一个主领域")
                                })
                                onClick { _, element ->
                                    val verify =
                                        e.block.location.toCenterLocation().toProxyLocation().toAABB(realmSize).getVertex().mapNotNull { vertex ->
                                            vertex.toLocation(e.block.world.name).toBukkitLocation().getRealm()
                                        }
                                    // 验证重合领域权限
                                    if (verify.any { !it.hasPermission("admin", e.player.name) }) {
                                        e.player.closeInventory()
//                                e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7当前位置与其他领域冲突.")
                                        e.player.info("§4:( §7当前位置与其他领域冲突.")
                                        e.block.type = Material.AIR
                                        e.player.inventory.addItem(item)
                                        return@onClick
                                    }
                                    // 验证选取领域
                                    val select = verify.firstOrNull { it == element }
                                    if (select == null) {
                                        e.player.closeInventory()
//                                e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7非法操作.")
                                        e.player.error("§4:( §7非法操作.")
                                        e.block.type = Material.AIR
                                        e.player.inventory.addItem(item)
                                        return@onClick
                                    }
                                    // 合并新建领域
                                    select.extends[e.block.location] = realmSize
                                    // 合并其他领域
                                    verify.forEach {
                                        if (it != select) {
                                            it.remove()
                                            select.extends[it.center] = it.size
                                            select.extends.putAll(it.extends)
                                        }
                                    }
                                    select.save()
                                    select.update()
                                    submit(async = true) {
                                        select.borderDisplay()
                                    }
                                    e.block.createDataContainer()["realms"] = Data(true)
                                    e.block.blockData = e.block.blockData.also {
                                        if (it is Waterlogged) {
                                            it.isWaterlogged = true
                                        }
                                    }
                                    e.player.closeInventory()
                                    e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
//                            e.player.sendHolographic(e.block.location.toCenterLocation().add(0.0, 1.0, 0.0), "§e:)", "§f领域已创建")
                                    e.player.info("§e:) §f领域已创建")
                                }

                                onGenerate { player, element, _, _ ->
                                    buildItem(XMaterial.PAPER) {
                                        name = "§f领域 ${element.center.blockX},${element.center.blockY},${element.center.blockZ}"
                                        lore += listOf("§7距离你 §e${Coerce.format(player.location.distance(element.center))} §7格",
                                            "§7点击与其合并")
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    fun Permission.register() {
        if (!permissions.contains(this)) {
            permissions.add(this)
        }
    }

    fun Location.getRealm(): RealmBlock? {
        return world.realms().firstOrNull { it.inside(this) }
    }

    fun getRealmBlock(location: Location): RealmBlock? {
        return location.getRealm()
    }

    fun Block.isRealmBlock(): Boolean {
        return getRealmBlock() != null
    }

    fun Block.getRealmBlock(): RealmBlock? {
        return world.realms().firstOrNull { it.center == location || it.extends.any { p -> p.key == location } }
    }

    fun ItemStack.getRealmSize(): Int {
        return if (this.isNotAir()) this.getItemTag().getOrElse("realm-size", ItemTagData(-1)).asInt() else -1
    }

    fun ItemStack.setRealmSize(value: Int) {
        val compound = this.getItemTag()
        compound["realm-size"] = ItemTagData(value)
        compound.saveTo(this)
    }

    fun World.realms(): List<RealmBlock> {
        return realmWorld().realms.values.flatten()
    }

    fun World.realmWorld(): RealmWorld {
        return worlds.computeIfAbsent(name) { RealmWorld() }
    }

    fun RealmBlock.create(player: Player) {
        users.computeIfAbsent(player.name) { HashMap() }["admin"] = true
        center.world.realmWorld().realms.computeIfAbsent(center.chunk.chunkKey) { ArrayList() }.add(this)
        this@RealmManager.permissions.forEach {
            permissions[it.id] = it.default
        }
        save()
    }

    fun RealmBlock.save() {
        center.chunk.persistentDataContainer.set(NamespacedKey(Realms.plugin, node), PersistentDataType.STRING, json)
    }

    fun RealmBlock.remove() {
        center.world.realmWorld().realms[center.chunk.chunkKey]?.remove(this)
        center.chunk.persistentDataContainer.remove(NamespacedKey(Realms.plugin, node))
    }

    fun RealmBlock.getAdmin(): String {
        return users.keys.firstOrNull { hasPermission("admin", it, false) }!!
    }

    fun RealmBlock.isAdmin(player: Player): Boolean {
        return getAdmin() == player.name || player.isOp
    }

    fun RealmBlock.editName(player: Player) {
        player.inputSign(arrayOf("", "", "↑请输入领域名称")) { les ->
            val names = "${les[0]}${les[1]}".screen()
            if (names.isEmpty()) {
                player.error("放弃了编辑!")
                return@inputSign
            }
            name = names
            player.info("当前领域名称更改为了 &f$names")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.editJoinTell(player: Player) {
        player.inputSign(arrayOf("", "", "↑请输入进入提示", "(第二行为子标题)")) { les ->
            if (les[0].isEmpty() && les[1].isEmpty()) {
                player.error("放弃了编辑!")
                openSettings(player)
                return@inputSign
            }
            val info = "${les[0]} | ${les[1]}".replace('§', '§')
            joinTell = info
            player.info("变更成功")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.editLeaveTell(player: Player) {
        player.inputSign(arrayOf("", "", "↑请输入离开提示", "(第二行为子标题)")) { les ->
            if (les[0].isEmpty() && les[1].isEmpty()) {
                player.error("放弃了编辑!")
                openSettings(player)
                return@inputSign
            }
            val info = "${les[0]} | ${les[1]}".replace('§', '§')
            leaveTell = info
            player.info("变更成功")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.open(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openMenu<Basic>("§f领域信息") {
            rows(3)
            map("", "#0#1#2#3#")
            set('0', buildItem(XMaterial.PAINTING) {
                name = "§f领域信息"
                lore.addAll(listOf(
                    "§7名称: §f${this@open.name}",
                    "§7持有者: §f${this@open.getAdmin()}",
                ))
            })
            set('1', buildItem(XMaterial.COMMAND_BLOCK) { name = "§f全局权限";lore += "§7将作用于所有玩家" })
            set('2', buildItem(XMaterial.CHAIN_COMMAND_BLOCK){ name = "§f个人权限";lore += "§7将作用于特定玩家" })
            set('3', buildItem(XMaterial.OBSERVER){ name = "§f领域设置";lore += "§7一些零散的设置" })
            onClick(true) {
                when (it.slot) {
                    '1' -> openPermissionWorld(player)
                    '2' -> openPermissionUsers(player)
                    '3' -> openSettings(player)
                }
            }
        }
    }

    fun RealmBlock.openSettings(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openMenu<Basic>("§f领域管理 [领域设置]") {
            rows(3)
            map("", "#012")
            set('0', buildItem(XMaterial.NAME_TAG) {
                name = "§f领域名称"
                lore.addAll(listOf(
                    "§7当前名称: §f${this@openSettings.name}",
                    "",
                    "§7点击编辑:",
                    "§8名称会在一些场景中用到"
                ))
            })
            set('1', buildItem(XMaterial.NAME_TAG) {
                name = "§f进入提示"
                lore.addAll(listOf(
                    "§7当前提示: §f${joinTell}",
                    "",
                    "§7点击编辑:",
                    "§8进入领地时的提示"
                ))
            })
            set('2', buildItem(XMaterial.NAME_TAG) {
                name = "§f离开提示"
                lore.addAll(listOf(
                    "§7当前提示: §f${leaveTell}",
                    "",
                    "§7点击编辑:",
                    "§8离开领地时的提示"
                ))
            })
            onClick(true) {
                when (it.slot) {
                    '0' -> editName(player)
                    '1' -> editJoinTell(player)
                    '2' -> editLeaveTell(player)
                }
            }
        }
    }

    fun RealmBlock.openPermissionWorld(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openMenu<Linked<Permission>>("领域管理 [全局权限]") {
            rows(6)
            elements {
                val list = RealmManager.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
                if (!player.isOp) {
                    list.removeAll(list.filter { it.adminSide })
                }
                list
            }
            slots(inventoryCenterSlots)
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f上一页" }
                else buildItem(XMaterial.ARROW) { name = "§8上一页"}
            }
            setNextPage(51) { _, hasNextPage ->
                if (hasNextPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f下一页" }
                else buildItem(XMaterial.ARROW) { name = "§8下一页"}
            }
            onClick { event, element ->
                if (element.adminSide && !player.isOp) {
                    event.clicker.error("该选项无效!")
                    return@onClick
                }
                permissions[element.id] = !hasPermission(element.id, def = element.default)
                player.openInventory(build())
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
                save()
            }
            onGenerate { player, element, _, _ ->
                if (element.adminSide && !player.isOp) {
                    ItemStack(Material.BARRIER)
                }
                element.generateMenuItem(hasPermission(element.id, def = element.default))
            }
        }
    }

    fun RealmBlock.openPermissionUsers(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openMenu<Linked<String>>("领域管理 [用户权限]") {
            rows(6)
            elements {
                users.keys.filter { it != player.name }.toList()
            }
            slots(inventoryCenterSlots)
            set(49, buildItem(XMaterial.WRITABLE_BOOK) {
                name = "§f添加用户"
                lore += "§7点击通过输入名称来添加用户"
            }) {
                player.closeInventory()
                player.inputSign(arrayOf("", "", "在第一行输入用户名称")) {
                    val playerExact = Bukkit.getPlayerExact(it[0])
                    when {
                        playerExact == null -> {
                            player.error("§c用户${it[0]}不在游戏")
                            return@inputSign
                        }
                        playerExact.name == player.name -> {
                            player.error("§c你不能添加自己")
                            return@inputSign
                        }
                        else -> {
                            users[playerExact.name] = HashMap()
                            save()
                            openPermissionUsers(player)
                        }
                    }
                }
            }

            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f上一页" }
                else buildItem(XMaterial.ARROW) { name = "§8上一页" }
            }
            setNextPage(51) { _, hasNextPage ->
                if (hasNextPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f下一页" }
                else buildItem(XMaterial.ARROW) { name = "§8下一页"}
            }
            onClick { _, element ->
                openPermissionUser(player, element)
            }
            onGenerate { _, element, _, _ ->
                if (hasPermission("admin", element)) {
                    buildItem(XMaterial.PLAYER_HEAD) { name = "§c管理员 $element";lore += "§7点击修改权限"; skullOwner = element }
                } else {
                    buildItem(XMaterial.PLAYER_HEAD) { name = "§f用户 $element";lore += "§7点击修改权限"; skullOwner = element }
                }
            }
        }
    }

    fun RealmBlock.openPermissionUser(player: Player, user: String) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openMenu<Linked<Permission>>("领域管理 [用户权限 : $user]") {
            rows(6)
            elements {
                val list = RealmManager.permissions.filter { it.playerSide }.sortedBy { it.priority }.toMutableList()
                list.toList().forEach {
                    if (it.adminSide && !player.isOp) {
                        list.remove(it)
                    }
                }
                list
            }
            slots(inventoryCenterSlots)
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f上一页" }
                else buildItem(XMaterial.ARROW) { name = "§8上一页" }
            }
            setNextPage(51) { _, hasNextPage ->
                if (hasNextPage) buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f下一页" }
                else buildItem(XMaterial.ARROW) { name = "§8下一页"}
            }
            set(49, buildItem(XMaterial.LAVA_BUCKET) {
                name = "§4删除用户"
                lore += "§c将该用户从当前领域中移除"
            }) {
                users.remove(user)
                save()
                openPermissionUsers(player)
            }
            onClick { _, element ->
                users[user]!![element.id] = !hasPermission(element.id, player = user, def = element.default)
                player.openInventory(build())
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
                save()
            }
            onGenerate { _, element, _, _ ->
                element.generateMenuItem(hasPermission(element.id, player = user, def = element.default))
            }
        }
    }

    fun Chunk.realms() = persistentDataContainer.keys.filter { it.key.startsWith("realm_") }.map { realm ->
        val position = realm.key.substring("realm_".length).split("_")
        val json = JsonParser().parse(persistentDataContainer[realm, PersistentDataType.STRING]).asJsonObject
        RealmBlock(position.toLocation(world), json["size"].asInt).also { realmBlock ->
            json["permissions"].asJsonObject.entrySet().forEach { (k, v) ->
                realmBlock.permissions[k] = v.asBoolean
            }
            json["users"].asJsonObject.entrySet().forEach { (k, v) ->
                realmBlock.users[k] = v.asJsonObject.entrySet().map { it.key to it.value.asBoolean }.toMap(HashMap())
            }
            json["extends"].asJsonObject.entrySet().forEach { (k, v) ->
                realmBlock.extends[k.split(",").toLocation(world)] = v.asInt
            }
            json["name"].asString.also { name ->
                realmBlock.name = name
            }
            json["joinTell"].asString.also { value ->
                realmBlock.joinTell = value
            }
            json["leaveTell"].asString.also { value ->
                realmBlock.leaveTell = value
            }
            realmBlock.update()
        }
    }

    private fun List<String>.toLocation(world: World): Location {
        return Location(world, Coerce.toDouble(this[0]), Coerce.toDouble(this[1]), Coerce.toDouble(this[2]))
    }

    @SubscribeEvent
    fun onJoinEvent(event: RealmsJoinEvent) {
        val realm = event.realmBlock ?: return
        submit(async = true) {
            realm.borderDisplay()
        }
        val message = realm.joinTell.split(" | ")
        if (message.size >= 2) {
            adaptPlayer(event.player).sendTitle(message[0], message[1], 20, 40, 20)
            return
        }
        adaptPlayer(event.player).sendTitle(message[0], "", 20, 40, 20)
    }

    @SubscribeEvent
    fun onLeaveEvent(event: RealmsLeaveEvent) {
        val realm = event.realmBlock ?: return
        submit(async = true) {
            realm.borderDisplay()
        }
        val message = realm.leaveTell.split(" | ")
        if (message.size >= 2) {
            adaptPlayer(event.player).sendTitle(message[0], message[1], 20, 40, 20)
            return
        }
        adaptPlayer(event.player).sendTitle(message[0], "", 20, 40, 20)
    }
}