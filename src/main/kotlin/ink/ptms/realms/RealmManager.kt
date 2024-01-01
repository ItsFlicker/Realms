package ink.ptms.realms

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.realms.data.Position.Companion.toPosition
import ink.ptms.realms.data.RealmBlock
import ink.ptms.realms.data.RealmWorld
import ink.ptms.realms.database.RealmDatabase
import ink.ptms.realms.hook.HookHuskTowns
import ink.ptms.realms.lib.redlib.event.DataBlockDestroyEvent
import ink.ptms.realms.lib.redlib.getDataContainer
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.Helper
import ink.ptms.realms.util.toAABB
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.sendTo
import taboolib.common.util.Vector
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
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

    private val registeredPermissions = ArrayList<Permission>()
    private val worlds = ConcurrentHashMap<String, RealmWorld>()
    val realms get() = worlds.values.flatMap { it.realms }

    @Awake(LifeCycle.ENABLE)
    fun init() {
        RealmDatabase.getByServerName(Realms.serverName!!).forEach {
            it.center.world.realmWorld().realms += it
        }
    }

    @Schedule(async = true, period = 30)
    fun particle() {
        Bukkit.getWorlds().forEach {
            it.realms().forEach { realm ->
                ProxyParticle.DOLPHIN.sendTo(realm.center.toCenterLocation().toProxyLocation(), 50.0, Vector(0.5, 0.5, 0.5), 5)

                realm.extend.forEach { (location, _) ->
                    ProxyParticle.REDSTONE.sendTo(location.toCenter().toProxyLocation(), 50.0, Vector(0.5, 0.5, 0.5), 5, data = ProxyParticle.DustData(Color(152, 249, 255), 1f))
                }
                if (realm.hasPermission("particle", def = false)) {
                    realm.particleDisplay()
                }
            }
        }

    }

    @SubscribeEvent
    fun onBreak(e: DataBlockDestroyEvent) {
        if (e.dataBlock.getBoolean("realms") && e.cause != DataBlockDestroyEvent.DestroyCause.PLAYER_BREAK) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onInteract(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock!!.isRealmBlock()) {
            e.isCancelled = true
            val realmBlock = e.clickedBlock!!.getRealmBlock()!!
            if (realmBlock.hasPermission("admin", e.player.name) || e.player.isOp) {
                realmBlock.open(e.player)
            } else {
                e.player.sendHolographic(e.clickedBlock!!.location.add(0.5, 1.0, 0.5), "§c:(", "§7这不属于你.")
            }
        }
    }


    @SubscribeEvent
    fun onPlayerBreak(e: BlockBreakEvent) {
        if (e.block.isRealmBlock()) {
            e.isCancelled = true
            val realmBlock = e.block.getRealmBlock()!!
            if (realmBlock.hasPermission("admin", e.player.name) || e.player.isOp) {
                // 破坏核心
                if (realmBlock.center == e.block.location) {
                    // 存在扩展
                    if (realmBlock.extend.isNotEmpty()) {
                        e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "§c:(", "§7需要先移除所有子领域.")
                    } else {
                        e.player.sendHolographic(e.block.location.toCenterLocation(), "§c:(", "§7领域已移除.")
                        e.block.world.playEffect(e.block.location.toCenterLocation(), Effect.STEP_SOUND, e.block.type)
                        e.block.type = Material.AIR
                        e.block.world.dropItemNaturally(e.block.location.toCenterLocation(), Realms.realmsDust.also {
                            it.amount = realmBlock.size
                        })
                        realmBlock.remove()
                    }
                }
                // 破坏扩展
                else if (realmBlock.extend.containsKey(e.block.location.toPosition())) {
                    e.block.world.playEffect(e.block.location.toCenterLocation(), Effect.STEP_SOUND, e.block.type)
                    e.block.type = Material.AIR
                    e.block.world.dropItemNaturally(e.block.location.toCenterLocation(), Realms.realmsDust.also {
                        it.amount = realmBlock.extend.remove(e.block.location.toPosition())!!
                    })
                    e.player.sendHolographic(e.block.location.toCenterLocation(), "§c:(", "§7子领域已移除.")
                    realmBlock.update()
                    realmBlock.save()
                }
            } else {
                e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "§c:(", "§7这不属于你.")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        val realmSize = e.itemInHand.getRealmSize()
        if (realmSize <= 0) {
            return
        }
        val item = e.itemInHand.clone().also { it.amount = 1 }
        if (e.player.gameMode == GameMode.SURVIVAL) {
            e.itemInHand.amount--
        }
        e.isCancelled = true
        val pt = e.blockPlaced.type
        val pd = e.blockPlaced.blockData.clone()
        val max = Realms.getPlayerMaxRealm(e.player)
        if (RealmDatabase.getByPlayer(e.player).size > max) {
            e.player.error("所持领域数量已达上限.")
            e.player.giveItem(item)
            return
        }
        if (HookHuskTowns.hasConflict(e.block.location.toCenterLocation())) {
            e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7当前位置与城镇冲突.")
            e.player.giveItem(item)
            return
        }
        val aabb = e.block.location.toCenterLocation().toAABB(realmSize)
        val realms = e.block.location.world.realms().filter { it.intersect(aabb) }
        when {
            realms.isEmpty() -> {
                val exist = RealmDatabase.getAll().map { it.name }
                var name = e.player.name
                var i = 2
                while (name in exist) {
                    name = e.player.name + i.toString()
                    i++
                }
                submit(delay = 4) {
                    e.block.type = pt
                    e.block.blockData = pd
                }
                RealmBlock(e.block.location, Realms.serverName!!, e.player.uniqueId, realmSize, name).create(e.player)
                e.block.getDataContainer()!!["realms"] = true
                e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "§e:)", "§f领域已创建")
            }
            realms.any { !it.hasPermission("admin", e.player.name) } -> {
                e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7当前位置与其他领域冲突.")
                e.player.giveItem(item)
            }
            else -> {
                e.player.giveItem(item)
                e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                // 领地合并界面
                e.player.openMenu<Linked<RealmBlock>>("领域合并") {
                    rows(6)
                    elements { realms }
                    slots(Slots.CENTER)
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
                        val verify = e.block.location.world.realms().filter { it.intersect(aabb) }
                        // 验证重合领域权限
                        if (verify.any { !it.hasPermission("admin", e.player.name) }) {
                            e.player.closeInventory()
                            e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7当前位置与其他领域冲突.")
                            return@onClick
                        }
                        // 验证选取领域
                        val select = verify.firstOrNull { it == element }
                        if (select == null) {
                            e.player.closeInventory()
                            e.player.sendHolographic(e.block.location.toCenterLocation(), "§4:(", "§7非法操作.")
                            return@onClick
                        }
                        // 合并新建领域
                        select.extend[e.block.location.toPosition()] = realmSize
                        // 合并其他领域
                        verify.forEach {
                            if (it != select) {
                                it.remove()
                                select.extend[it.center.toPosition()] = it.size
                                select.extend.putAll(it.extend)
                            }
                        }
                        select.save()
                        select.update()
                        submitAsync {
                            select.particleDisplay()
                        }
                        e.player.checkItem(item, 1, true)
                        e.block.type = pt
                        e.block.blockData = pd
                        e.block.getDataContainer()!!["realms"] = true
                        e.player.closeInventory()
                        e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                        e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "§e:)", "§f领域已创建")
                    }
                    onGenerate { player, element, _, _ ->
                        buildItem(XMaterial.PAPER) {
                            name = "§f领域 ${element.center.blockX},${element.center.blockY},${element.center.blockZ}"
                            lore += listOf("§7距离你 §e${Coerce.format(player.location.distance(element.center))} §7格", "§7点击与其合并")
                        }
                    }
                }
            }
        }
    }

    fun Permission.register() {
        if (!registeredPermissions.contains(this)) {
            registeredPermissions.add(this)
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
        return world.realms().firstOrNull { it.center == location || it.extend.any { p -> p.key == location.toPosition() } }
    }

    fun ItemStack.getRealmSize(): Int {
        return if (this.isNotAir()) getItemTag().getOrElse("realm-size", ItemTagData(-1)).asInt() else -1
    }

    fun ItemStack.setRealmSize(value: Int) {
        val compound = getItemTag()
        compound["realm-size"] = ItemTagData(value)
        compound.saveTo(this)
    }

    fun World.realms(): List<RealmBlock> {
        return realmWorld().realms
    }

    fun World.realmWorld(): RealmWorld {
        return worlds.computeIfAbsent(name) { RealmWorld() }
    }

    fun RealmBlock.create(player: Player) {
        users.computeIfAbsent(player.name) { HashMap() }["admin"] = true
        center.world.realmWorld().realms += this
        this@RealmManager.registeredPermissions.forEach {
            permissions[it.id] = it.default
        }
        save()
    }

    fun RealmBlock.save() {
        RealmDatabase.update(this)
    }

    fun RealmBlock.remove() {
        center.world.realmWorld().realms -= this
        RealmDatabase.delete(this)
    }

    fun RealmBlock.getAdmins(): List<String> {
        return users.keys.filter { hasPermission("admin", it, false) }
    }

    fun RealmBlock.isAdmin(player: Player): Boolean {
        return player.name in getAdmins() || player.isOp
    }

    fun RealmBlock.editName(player: Player) {
        player.inputSign(arrayOf("", "", "↑请输入领域名称", "")) {
            val names = "${it[0]}${it[1]}".screen()
            if (names.isEmpty() || names == name) {
                player.error("放弃了编辑!")
                player.closeInventory()
                return@inputSign
            }
            if (RealmDatabase.getAll().any { realm -> realm.name == names }) {
                player.error("领域名与其他领域冲突!")
                player.closeInventory()
                return@inputSign
            }
            name = names
            player.info("当前领域名称更改为了 &f$names")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.editJoinTell(player: Player) {
        player.inputSign(arrayOf("", "", "↑请输入进入提示", "(第二行为子标题)")) {
            val info = if (it[0].isEmpty() && it[1].isEmpty()) {
                ""
            } else {
                "${it[0]} | ${it[1]}".colored()
            }
            joinMessage = info
            player.info("变更成功")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.editLeaveTell(player: Player) {
        player.inputSign(arrayOf("", "", "↑请输入离开提示", "(第二行为子标题)")) {
            val info = if (it[0].isEmpty() && it[1].isEmpty()) {
                ""
            } else {
                "${it[0]} | ${it[1]}".colored()
            }
            leaveMessage = info
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
                    "§7持有者: §f${this@open.getAdmins().joinToString(",")}",
                    "§7服务器: §f${this@open.serverName}"
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
            map(
                "",
                "#012",
                ""
            )
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
                    "§7当前提示: §f${joinMessage}",
                    "",
                    "§7点击编辑:",
                    "§8进入领地时的提示"
                ))
            })
            set('2', buildItem(XMaterial.NAME_TAG) {
                name = "§f离开提示"
                lore.addAll(listOf(
                    "§7当前提示: §f${leaveMessage}",
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
                val list = registeredPermissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
                if (!player.isOp) {
                    list.removeAll(list.filter { it.adminSide })
                }
                list
            }
            slots(Slots.CENTER)
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
            elements { users.keys.filter { it != player.name } }
            slots(Slots.CENTER)
            set(49, buildItem(XMaterial.WRITABLE_BOOK) {
                name = "§f添加用户"
                lore += "§7点击通过输入名称来添加用户"
            }) {
                player.closeInventory()
                player.info("在聊天框输入用户名称")
                player.nextChat {
                    val playerExact = Bukkit.getPlayerExact(it)
                    when {
                        playerExact == null -> {
                            player.error("§c用户${it[0]}不在游戏")
                        }
                        playerExact.name == player.name -> {
                            player.error("§c你不能添加自己")
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
                registeredPermissions.filter { it.playerSide }.sortedBy { it.priority }.filterNot { it.adminSide && !player.isOp }
            }
            slots(Slots.CENTER)
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

    private fun Player.sendHolographic(location: Location, vararg message: String) {
        Adyeshach.api().getHologramHandler().sendHologramMessage(this, location, message.toList())
    }
}