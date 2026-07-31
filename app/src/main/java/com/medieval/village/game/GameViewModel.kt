package com.medieval.village.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.medieval.village.model.DungeonFactory
import com.medieval.village.model.DungeonFloor
import com.medieval.village.model.DungeonMonster
import com.medieval.village.model.DungeonTile
import com.medieval.village.model.EQUIP_SLOTS
import com.medieval.village.model.EquippedItem
import com.medieval.village.model.InventoryEntry
import com.medieval.village.model.Item
import com.medieval.village.model.ItemCatalog
import com.medieval.village.model.ItemType
import com.medieval.village.model.Mercenary
import com.medieval.village.model.Place
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Player
import com.medieval.village.model.PubNpc
import com.medieval.village.model.PubNpcCatalog
import com.medieval.village.model.Skill
import com.medieval.village.model.Village
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

enum class Facing { DOWN, UP, LEFT, RIGHT }

enum class Scene { VILLAGE, INTERIOR }

enum class MenuTab { NONE, STATUS, INVENTORY, EQUIPMENT, SYSTEM }

private data class Waypoint(val x: Float, val y: Float)

class GameViewModel : ViewModel() {

    companion object {
        private const val WALK_SPEED = 360f
        private const val DUNGEON_WALK_SPEED = 280f
        private const val DUNGEON_TOUCH_RANGE = 52f
        const val MAX_ACTIVE_MERCENARY = 2
    }

    var player by mutableStateOf(Player())
        private set

    val inventory = mutableStateListOf<InventoryEntry>()
    val equipment = mutableStateMapOf<ItemType, EquippedItem>()
    val skills = mutableStateListOf<Skill>()
    /** 고용한 전체 용병 명단 */
    val party = mutableStateListOf<Mercenary>()
    /** Status에서 원정대로 선택한 용병 id (최대 2명) */
    val activeMercenaryIds = mutableStateListOf<String>()

    /** 현재 화면에서 보여줄 대사/결과 로그 */
    val log = mutableStateListOf<String>()

    var scene by mutableStateOf(Scene.INTERIOR)
        private set
    var currentPlace by mutableStateOf<PlaceId?>(PlaceId.HOME)
        private set
    var menuTab by mutableStateOf(MenuTab.NONE)

    var heroX by mutableFloatStateOf(Village.of(PlaceId.HOME).doorX)
        private set
    var heroY by mutableFloatStateOf(Village.of(PlaceId.HOME).doorY)
        private set
    var facing by mutableStateOf(Facing.DOWN)
        private set
    var walking by mutableStateOf(false)
        private set
    var walkPhase by mutableFloatStateOf(0f)
        private set

    var pubHeroX by mutableFloatStateOf(500f)
        private set
    var pubHeroY by mutableFloatStateOf(610f)
        private set
    var pubWalking by mutableStateOf(false)
        private set
    var pubDialogue by mutableStateOf<String?>(null)
        private set
    var pubSpeakerId by mutableStateOf<String?>(null)
        private set

    /** 대련소 전적 */
    var arenaWins by mutableStateOf(0)
        private set
    var arenaLosses by mutableStateOf(0)
        private set

    /** 라그나로크식 걸어다니는 던전 상태 */
    var dungeonFloor by mutableStateOf<DungeonFloor?>(null)
        private set
    var dungeonFloorNumber by mutableStateOf(1)
        private set
    var dungeonHeroX by mutableFloatStateOf(0f)
        private set
    var dungeonHeroY by mutableFloatStateOf(0f)
        private set
    var dungeonWalking by mutableStateOf(false)
        private set
    var dungeonHint by mutableStateOf("")
        private set
    /** UI에서 재생할 일회성 효과음 신호 */
    var sfxSignal by mutableStateOf(0)
        private set
    var lastSfx by mutableStateOf<String?>(null)
        private set

    private val path = ArrayDeque<Waypoint>()
    private var pendingEnter: PlaceId? = null
    private var pubTarget: Waypoint? = null
    private var pendingPubNpc: PubNpc? = null
    private var dungeonTarget: Waypoint? = null
    private var pendingDungeonMonster: DungeonMonster? = null
    private var dungeonCombatLock = false
    private var monsterWanderAcc = 0f

    init {
        newGame()
    }

    // ---------------------------------------------------------------- 세이브/리셋

    fun newGame() {
        player = Player()
        inventory.clear()
        equipment.clear()
        skills.clear()
        party.clear()
        activeMercenaryIds.clear()
        log.clear()
        path.clear()
        pendingEnter = null
        pubTarget = null
        pendingPubNpc = null
        arenaWins = 0
        arenaLosses = 0
        clearDungeonState()

        addItem(ItemCatalog.potion, 3)
        addItem(ItemCatalog.bread, 2)
        equipment[ItemType.WEAPON] = EquippedItem(ItemCatalog.rustySword)
        equipment[ItemType.ARMOR] = EquippedItem(ItemCatalog.leatherArmor)

        val home = Village.of(PlaceId.HOME)
        heroX = home.doorX
        heroY = home.doorY
        facing = Facing.DOWN
        walking = false
        pubHeroX = 500f
        pubHeroY = 610f
        pubWalking = false
        pubDialogue = null
        pubSpeakerId = null
        scene = Scene.INTERIOR
        currentPlace = PlaceId.HOME
        menuTab = MenuTab.NONE
        say("풍요의 마을… 한때 '신성한 포도주'로 번영했던 이곳에 눈을 떴다.")
        say("몇 년 전 지하 최심부에서 검붉은 '좀비석'이 발굴된 뒤, 마을은 저주에 잠식되고 있다.")
        say("문을 열고, 지상으로 스며드는 재앙의 근원을 마주하자.")
    }

    // ---------------------------------------------------------------- 스탯 계산

    val equipAtk: Int get() = equipment.values.sumOf { it.atk }
    val equipDef: Int get() = equipment.values.sumOf { it.def }
    val skillPower: Int get() = skills.sumOf { it.power } / 2
    val activeParty: List<Mercenary>
        get() = activeMercenaryIds.mapNotNull { id -> party.firstOrNull { it.id == id } }
    val partyPower: Int get() = activeParty.sumOf { it.power }

    val totalAtk: Int get() = player.baseAtk + equipAtk + player.str / 2 + skillPower
    val totalDef: Int get() = player.baseDef + equipDef + player.agi / 3

    // ---------------------------------------------------------------- 이동

    fun tick(dt: Float) {
        if (scene == Scene.INTERIOR && currentPlace == PlaceId.PUB) {
            tickPub(dt)
            return
        }
        if (scene == Scene.INTERIOR && currentPlace == PlaceId.DUNGEON) {
            tickDungeon(dt)
            return
        }
        if (scene != Scene.VILLAGE) return

        val target = path.firstOrNull()
        if (target == null) {
            if (walking) walking = false
            val enter = pendingEnter
            if (enter != null) {
                pendingEnter = null
                enterPlace(enter)
            }
            return
        }

        walking = true
        walkPhase += dt * 10f

        val dx = target.x - heroX
        val dy = target.y - heroY
        val dist = hypot(dx, dy)
        val step = WALK_SPEED * dt

        if (dist <= step || dist < 0.01f) {
            heroX = target.x
            heroY = target.y
            path.removeFirst()
        } else {
            heroX += dx / dist * step
            heroY += dy / dist * step
            facing = if (abs(dx) > abs(dy)) {
                if (dx > 0) Facing.RIGHT else Facing.LEFT
            } else {
                if (dy > 0) Facing.DOWN else Facing.UP
            }
        }
    }

    private fun tickPub(dt: Float) {
        val target = pubTarget
        if (target == null) {
            pubWalking = false
            return
        }
        val dx = target.x - pubHeroX
        val dy = target.y - pubHeroY
        val dist = hypot(dx, dy)
        val step = WALK_SPEED * dt
        if (dist <= step || dist < 0.01f) {
            pubHeroX = target.x
            pubHeroY = target.y
            pubTarget = null
            pubWalking = false
            pendingPubNpc?.let { speakTo(it) }
            pendingPubNpc = null
        } else {
            pubHeroX += dx / dist * step
            pubHeroY += dy / dist * step
            pubWalking = true
            walkPhase += dt * 10f
            facing = if (abs(dx) > abs(dy)) {
                if (dx > 0) Facing.RIGHT else Facing.LEFT
            } else {
                if (dy > 0) Facing.DOWN else Facing.UP
            }
        }
    }

    fun walkInPub(x: Float, y: Float) {
        pubDialogue = null
        pubSpeakerId = null
        pendingPubNpc = null
        pubTarget = Waypoint(
            x.coerceIn(90f, PubNpcCatalog.WORLD_W - 90f),
            y.coerceIn(180f, PubNpcCatalog.WORLD_H - 45f)
        )
    }

    fun approachPubNpc(npc: PubNpc) {
        pubDialogue = null
        pubSpeakerId = null
        pendingPubNpc = npc
        pubTarget = Waypoint(
            npc.x,
            (npc.y + 95f).coerceAtMost(PubNpcCatalog.WORLD_H - 40f)
        )
    }

    private fun speakTo(npc: PubNpc) {
        pubSpeakerId = npc.id
        pubDialogue = npc.lines.random()
        say("${npc.name}: ${pubDialogue}")
    }

    /** 빈 땅을 눌렀을 때: 길을 따라 그 지점까지 걸어간다. */
    fun walkTo(x: Float, y: Float) {
        pendingEnter = null
        buildPath(x.coerceIn(40f, Village.W - 40f), y.coerceIn(120f, Village.H - 40f))
    }

    /** 건물을 눌렀을 때: 문 앞까지 걸어간 뒤 자동으로 입장한다. */
    fun goToPlace(place: Place) {
        if (scene != Scene.VILLAGE) return
        val atDoor = abs(heroX - place.doorX) < 6f && abs(heroY - place.doorY) < 6f
        if (atDoor) {
            enterPlace(place.id)
            return
        }
        buildPath(place.doorX, place.doorY)
        pendingEnter = place.id
    }

    /** 마을 길(중앙 대로)을 경유하는 ㄱ자 경로를 만든다. */
    private fun buildPath(tx: Float, ty: Float) {
        path.clear()
        val sameRow = abs(heroY - ty) < 2f
        val onMainRoad = abs(heroX - Village.ROAD_X) < 2f && abs(tx - Village.ROAD_X) < 2f
        if (sameRow || onMainRoad) {
            path.addLast(Waypoint(tx, ty))
        } else {
            path.addLast(Waypoint(Village.ROAD_X, heroY))
            path.addLast(Waypoint(Village.ROAD_X, ty))
            path.addLast(Waypoint(tx, ty))
        }
    }

    // ---------------------------------------------------------------- 장소 출입

    fun enterPlace(id: PlaceId) {
        currentPlace = id
        scene = Scene.INTERIOR
        menuTab = MenuTab.NONE
        log.clear()
        if (id == PlaceId.PUB) {
            pubHeroX = 500f
            pubHeroY = 610f
            pubTarget = null
            pendingPubNpc = null
            pubWalking = false
            pubDialogue = null
            pubSpeakerId = null
        }
        if (id == PlaceId.DUNGEON) {
            enterDungeonFloor(1)
            emitSfx("door")
        }
        say(greetingOf(id))
    }

    fun leavePlace() {
        val id = currentPlace ?: return
        val place = Village.of(id)
        heroX = place.doorX
        heroY = place.doorY
        facing = Facing.DOWN
        path.clear()
        pendingEnter = null
        pubTarget = null
        pendingPubNpc = null
        walking = false
        pubWalking = false
        if (id == PlaceId.DUNGEON) clearDungeonState()
        scene = Scene.VILLAGE
        currentPlace = null
        menuTab = MenuTab.NONE
    }

    private fun greetingOf(id: PlaceId): String = when (id) {
        PlaceId.HOME -> "창문 너머로도 하수구 냄새가 스며든다. 그래도 여기는 나의 오두막이다."
        PlaceId.SHOP -> "\"어서 오세요… 횃불이랑 붕대는 늘 비치해 둡니다. 요즘엔 필수죠.\""
        PlaceId.WEAPON_SHOP -> "\"좀비 뼈라도 가를 쇠를 찾나? 잘 왔네.\""
        PlaceId.HOSPITAL -> "\"물린 상처입니까, 아니면… 좀비석 기운입니까?\""
        PlaceId.CHURCH -> "\"저주가 지상을 핥고 있소. 빛의 가호가 그대와 함께하기를.\""
        PlaceId.INN -> "\"문은 꼭 잠그세요. 밤엔 하수도 쪽에서 기척이 들립니다.\""
        PlaceId.PUB -> "포도주 향 사이로, 좀비석과 영주를 향한 낮은 원성이 섞여 들린다."
        PlaceId.ARENA -> "\"지상에서라도 칼날을 갈아야지. 지하에선 실수가 곧 죽음이야.\""
        PlaceId.DUNGEON -> "축축한 하수도 바람이 얼굴을 스친다. 저주의 둥지가 발밑에서 숨 쉰다."
        PlaceId.BLACKSMITH -> "\"좀비 이빨에 안 깨지려면, 쇠는 더 두들겨야지.\""
        PlaceId.MAGIC_SCHOOL -> "\"연금술사들이 손을 댄 그 돌… 우리는 이제 해독만 연구한다네.\""
        PlaceId.MERCENARY -> "\"좀비 둥지 안내라면 돈만 주면 붙여주지. 목숨값은 별도야.\""
    }

    private fun emitSfx(name: String) {
        lastSfx = name
        sfxSignal++
    }

    fun say(msg: String) {
        log.add(msg)
        if (log.size > 40) log.removeAt(0)
    }

    // ---------------------------------------------------------------- 인벤토리

    fun addItem(item: Item, count: Int = 1) {
        val idx = inventory.indexOfFirst { it.item.id == item.id }
        if (idx >= 0) {
            inventory[idx] = inventory[idx].copy(count = inventory[idx].count + count)
        } else {
            inventory.add(InventoryEntry(item, count))
        }
    }

    fun removeItem(item: Item, count: Int = 1) {
        val idx = inventory.indexOfFirst { it.item.id == item.id }
        if (idx < 0) return
        val left = inventory[idx].count - count
        if (left <= 0) inventory.removeAt(idx) else inventory[idx] = inventory[idx].copy(count = left)
    }

    fun useItem(item: Item): Boolean {
        if (item.type != ItemType.CONSUMABLE) return false
        if (item.healHp == 0 && item.healMp == 0) {
            say("${item.name}은(는) 지금 쓸 수 없다.")
            return false
        }
        val before = player.hp to player.mp
        player = player.copy(
            hp = (player.hp + item.healHp).coerceAtMost(player.maxHp),
            mp = (player.mp + item.healMp).coerceAtMost(player.maxMp)
        )
        removeItem(item)
        val dHp = player.hp - before.first
        val dMp = player.mp - before.second
        say("${item.name}을(를) 사용했다. " + buildString {
            if (dHp > 0) append("HP +$dHp ")
            if (dMp > 0) append("MP +$dMp")
            if (dHp == 0 && dMp == 0) append("효과가 없었다.")
        }.trim())
        return true
    }

    fun equip(item: Item) {
        if (!item.isEquipment) return
        val prev = equipment[item.type]
        equipment[item.type] = EquippedItem(item)
        removeItem(item)
        if (prev != null) {
            addItem(prev.item)
            say("${prev.displayName}을(를) 벗고 ${item.name}을(를) 장착했다.")
        } else {
            say("${item.name}을(를) 장착했다.")
        }
    }

    fun unequip(type: ItemType) {
        val cur = equipment.remove(type) ?: return
        addItem(cur.item)
        say("${cur.displayName}을(를) 해제했다.")
    }

    // ---------------------------------------------------------------- 상거래

    fun buy(item: Item): Boolean {
        if (player.gold < item.price) {
            say("골드가 부족하다. (${item.price}G 필요)")
            return false
        }
        player = player.copy(gold = player.gold - item.price)
        addItem(item)
        say("${item.name}을(를) 샀다. (-${item.price}G)")
        return true
    }

    fun sell(entry: InventoryEntry) {
        val price = entry.item.sellPrice
        removeItem(entry.item)
        player = player.copy(gold = player.gold + price)
        say("${entry.item.name}을(를) 팔았다. (+${price}G)")
    }

    // ---------------------------------------------------------------- 집

    fun sleepAtHome() {
        player = player.copy(
            hp = player.maxHp,
            mp = player.maxMp,
            day = player.day + 1,
            blessing = (player.blessing - 1).coerceAtLeast(0)
        )
        say("깊이 잠들었다. ${player.day}일차 아침이 밝았다. (HP·MP 완전 회복)")
    }

    // ---------------------------------------------------------------- 병원

    fun hospitalHealCost(): Int = ((player.maxHp - player.hp) * 2).coerceAtLeast(0)

    fun hospitalHeal() {
        val cost = hospitalHealCost()
        if (cost == 0) {
            say("\"멀쩡하시군요. 그냥 돌아가셔도 됩니다.\"")
            return
        }
        if (player.gold < cost) {
            say("치료비가 부족하다. (${cost}G 필요)")
            return
        }
        player = player.copy(gold = player.gold - cost, hp = player.maxHp)
        say("치료를 받았다. HP가 모두 회복되었다. (-${cost}G)")
    }

    fun hospitalTonic() {
        val cost = 150
        if (player.gold < cost) {
            say("골드가 부족하다. (${cost}G 필요)")
            return
        }
        player = player.copy(gold = player.gold - cost, maxHp = player.maxHp + 6, hp = player.hp + 6)
        say("영양제를 처방받았다. 최대 HP가 6 늘었다. (-${cost}G)")
    }

    // ---------------------------------------------------------------- 교회

    fun pray() {
        val recover = (player.maxMp * 0.35f).toInt().coerceAtLeast(3)
        player = player.copy(mp = (player.mp + recover).coerceAtMost(player.maxMp))
        say("조용히 기도를 올렸다. MP가 ${recover} 회복되었다.")
        if (Random.nextInt(100) < 20) {
            player = player.copy(luck = player.luck + 1)
            say("마음이 맑아진다. 행운이 1 올랐다!")
        }
    }

    fun donate(amount: Int) {
        if (player.gold < amount) {
            say("헌금할 골드가 부족하다.")
            return
        }
        player = player.copy(gold = player.gold - amount, blessing = player.blessing + 3)
        say("${amount}G를 헌금했다. 3일간 저주를 밀어내는 축복을 받는다. (전투력 상승)")
    }

    // ---------------------------------------------------------------- 여관

    fun stayAtInn() {
        val cost = 60
        if (player.gold < cost) {
            say("숙박비가 부족하다. (${cost}G 필요)")
            return
        }
        player = player.copy(
            gold = player.gold - cost,
            hp = player.maxHp,
            mp = player.maxMp,
            day = player.day + 1,
            blessing = (player.blessing - 1).coerceAtLeast(0)
        )
        say("푹신한 침대에서 하룻밤. HP·MP가 모두 회복되었다. (-${cost}G, ${player.day}일차)")
    }

    private val rumors = listOf(
        "\"최하층에서 캐낸 검붉은 돌… 사람들이 그걸 '좀비석'이라 부르지.\"",
        "\"영주와 연금술사들이 병도 고치고 목숨도 늘리겠다고 그 돌을 만졌어. 결과는… 저주뿐이야.\"",
        "\"오염된 사람들은 죽지도 못한 채 뇌가 썩고, 생살 허기만 남았다대.\"",
        "\"지하 보관소랑 하수도가 통째로 좀비 둥지가 됐어. 저주가 지상까지 스며 나온다니까.\"",
        "\"예전엔 신성한 포도주로 이 마을이 부유했는데… 지금은 술잔에도 그 그림자가 비친다.\"",
        "\"교회에서는 좀비석 기운을 씻는 기도를 올린다더군. 헌금하면 며칠은 마음이 든든해진대.\""
    )

    fun listenRumor() {
        say(rumors.random())
    }

    // ---------------------------------------------------------------- 대장간

    fun upgrade(type: ItemType) {
        val cur = equipment[type]
        if (cur == null) {
            say("강화할 ${type.label}을(를) 착용하고 있지 않다.")
            return
        }
        if (cur.plus >= 9) {
            say("\"이 이상은 내 솜씨로 안 되네.\"")
            return
        }
        val cost = cur.upgradeCost
        if (player.gold < cost) {
            say("강화 비용이 부족하다. (${cost}G 필요)")
            return
        }
        player = player.copy(gold = player.gold - cost)
        val successRate = (95 - cur.plus * 8).coerceAtLeast(35)
        if (Random.nextInt(100) < successRate) {
            equipment[type] = cur.copy(plus = cur.plus + 1)
            say("모루 위에서 불꽃이 튄다. ${cur.item.name} +${cur.plus + 1} 강화 성공! (-${cost}G)")
        } else {
            say("쇠가 울지 않는다... 강화에 실패했다. (-${cost}G)")
        }
    }

    // ---------------------------------------------------------------- 마법학교

    fun learn(skill: Skill) {
        if (skills.any { it.id == skill.id }) {
            say("이미 익힌 마법이다.")
            return
        }
        if (player.gold < skill.cost) {
            say("수업료가 부족하다. (${skill.cost}G 필요)")
            return
        }
        player = player.copy(gold = player.gold - skill.cost, maxMp = player.maxMp + 5, mp = player.mp + 5)
        skills.add(skill)
        say("${skill.name}을(를) 익혔다! 최대 MP가 5 늘었다. (-${skill.cost}G)")
    }

    fun study() {
        val cost = 60 + player.intel * 12
        if (player.gold < cost) {
            say("수업료가 부족하다. (${cost}G 필요)")
            return
        }
        player = player.copy(gold = player.gold - cost, intel = player.intel + 1)
        say("고서를 파고들었다. 지능이 1 올랐다. (-${cost}G)")
    }

    // ---------------------------------------------------------------- 용병고용소

    fun hire(merc: Mercenary) {
        if (party.any { it.id == merc.id }) {
            say("이미 고용한 용병이다.")
            return
        }
        if (player.gold < merc.cost) {
            say("계약금이 부족하다. (${merc.cost}G 필요)")
            return
        }
        player = player.copy(gold = player.gold - merc.cost)
        party.add(merc)
        if (activeMercenaryIds.size < MAX_ACTIVE_MERCENARY) {
            activeMercenaryIds.add(merc.id)
            say("${merc.name}이(가) 동료가 되어 원정대에 합류했다! (-${merc.cost}G)")
        } else {
            say("${merc.name}을(를) 고용했다. Status에서 원정대를 편성할 수 있다. (-${merc.cost}G)")
        }
    }

    fun dismiss(merc: Mercenary) {
        party.removeAll { it.id == merc.id }
        activeMercenaryIds.remove(merc.id)
        say("${merc.name}과(와) 작별했다.")
    }

    fun toggleMercenaryActive(merc: Mercenary) {
        if (merc.id in activeMercenaryIds) {
            activeMercenaryIds.remove(merc.id)
            say("${merc.name}을(를) 원정대에서 대기시켰다.")
            return
        }
        if (activeMercenaryIds.size >= MAX_ACTIVE_MERCENARY) {
            say("원정대는 최대 ${MAX_ACTIVE_MERCENARY}명까지 선택할 수 있다.")
            return
        }
        activeMercenaryIds.add(merc.id)
        say("${merc.name}이(가) 원정대에 합류했다.")
    }

    // ---------------------------------------------------------------- 대련소

    fun spar() {
        if (player.hp < player.maxHp * 0.2f) {
            say("\"그 몰골로는 안 되네. 몸부터 추스르게.\"")
            return
        }
        val rivalLevel = (player.level + Random.nextInt(-1, 2)).coerceAtLeast(1)
        val rivalName = listOf("훈련병 톰", "용병 카일", "기사 에드윈", "떠돌이 무사", "산적 두목").random()
        val myScore = totalAtk + Random.nextInt(0, 12) + blessBonus()
        val rivalScore = 8 + rivalLevel * 7 + Random.nextInt(0, 12)

        say("$rivalName 과(와) 대련을 시작한다!")
        if (myScore >= rivalScore) {
            arenaWins++
            val gold = 25 + rivalLevel * 12
            val exp = 18 + rivalLevel * 10
            player = player.copy(gold = player.gold + gold, hp = (player.hp - Random.nextInt(2, 8)).coerceAtLeast(1))
            say("승리했다! (+${gold}G, EXP +$exp)")
            gainExp(exp)
        } else {
            arenaLosses++
            val dmg = 8 + rivalLevel * 3
            say("패배했다... 목검에 크게 얻어맞았다. (HP -$dmg)")
            applyDamage(dmg)
            gainExp(6)
        }
    }

    // ---------------------------------------------------------------- 던전 (라그나로크식 도보 탐험)

    private fun clearDungeonState() {
        dungeonFloor = null
        dungeonFloorNumber = 1
        dungeonHeroX = 0f
        dungeonHeroY = 0f
        dungeonWalking = false
        dungeonHint = ""
        dungeonTarget = null
        pendingDungeonMonster = null
        dungeonCombatLock = false
        monsterWanderAcc = 0f
    }

    /** 몬스터 위치/사망 등 내부 변이 후 Compose 재구성을 유도한다. */
    private fun refreshDungeonFloor() {
        val map = dungeonFloor ?: return
        dungeonFloor = map.copy(monsters = map.monsters.toMutableList())
    }

    private fun enterDungeonFloor(floor: Int) {
        val map = DungeonFactory.generate(floor)
        dungeonFloor = map
        dungeonFloorNumber = floor
        dungeonHeroX = map.spawnX
        dungeonHeroY = map.spawnY
        dungeonWalking = false
        dungeonTarget = null
        pendingDungeonMonster = null
        dungeonCombatLock = false
        dungeonHint = "stairs_up"
        if (floor > player.dungeonDepth) {
            player = player.copy(dungeonDepth = floor)
        }
        say("─── 지하 ${floor}층 · 오염된 통로 ───")
        if (floor == 1) {
            say("한때 포도주 보관소와 하수도였던 길이 좀비의 숨결로 가득하다.")
        } else {
            say("더 깊은 곳에서 검붉은 기운이 피부를 찌른다. 좀비석이 가까워지는 기분이다.")
        }
    }

    fun walkInDungeon(x: Float, y: Float) {
        val map = dungeonFloor ?: return
        if (dungeonCombatLock) return
        pendingDungeonMonster = null
        val tx = x.coerceIn(DungeonFactory.TILE, map.worldW - DungeonFactory.TILE)
        val ty = y.coerceIn(DungeonFactory.TILE, map.worldH - DungeonFactory.TILE)
        if (!map.isWalkable(tx, ty)) return
        dungeonTarget = Waypoint(tx, ty)
    }

    fun approachDungeonMonster(monster: DungeonMonster) {
        if (!monster.alive || dungeonCombatLock) return
        pendingDungeonMonster = monster
        dungeonTarget = Waypoint(monster.x, monster.y)
    }

    fun descendDungeon() {
        val map = dungeonFloor ?: return
        if (map.tileKindAt(dungeonHeroX, dungeonHeroY) != DungeonTile.STAIRS_DOWN) {
            say("아래층으로 이어지는 계단 위에 서야 한다.")
            return
        }
        if (player.hp <= player.maxHp * 0.15f) {
            say("몸 상태로는 더 내려갈 수 없다. 물약을 쓰거나 지상으로 돌아가자.")
            return
        }
        emitSfx("door")
        enterDungeonFloor(dungeonFloorNumber + 1)
    }

    fun escapeDungeon() {
        say("지상의 공기가 폐를 채운다. 저주는 아직 지하에 웅크리고 있다.")
        emitSfx("door")
        leavePlace()
    }

    private fun tickDungeon(dt: Float) {
        val map = dungeonFloor ?: return
        updateDungeonHint(map)
        wanderMonsters(map, dt)
        checkDungeonContact(map)

        val target = dungeonTarget
        if (target == null) {
            dungeonWalking = false
            return
        }

        val dx = target.x - dungeonHeroX
        val dy = target.y - dungeonHeroY
        val dist = hypot(dx, dy)
        val step = DUNGEON_WALK_SPEED * dt
        if (dist <= step || dist < 0.01f) {
            tryMoveDungeon(map, target.x, target.y)
            dungeonTarget = null
            dungeonWalking = false
            pendingDungeonMonster?.let { fightDungeonMonster(it) }
            pendingDungeonMonster = null
            return
        }

        val nx = dungeonHeroX + dx / dist * step
        val ny = dungeonHeroY + dy / dist * step
        val moved = tryMoveDungeon(map, nx, ny)
        if (!moved) {
            // 벽에 막히면 축 분리 시도 (라그나로크식 미끄러짐)
            val movedX = tryMoveDungeon(map, nx, dungeonHeroY)
            val movedY = tryMoveDungeon(map, dungeonHeroX, ny)
            if (!movedX && !movedY) {
                dungeonTarget = null
                dungeonWalking = false
                pendingDungeonMonster = null
                return
            }
        }
        dungeonWalking = true
        walkPhase += dt * 10f
        facing = if (abs(dx) > abs(dy)) {
            if (dx > 0) Facing.RIGHT else Facing.LEFT
        } else {
            if (dy > 0) Facing.DOWN else Facing.UP
        }
        pendingDungeonMonster?.let { monster ->
            if (monster.alive && hypot(dungeonHeroX - monster.x, dungeonHeroY - monster.y) < DUNGEON_TOUCH_RANGE) {
                dungeonTarget = null
                dungeonWalking = false
                fightDungeonMonster(monster)
                pendingDungeonMonster = null
            }
        }
    }

    private fun tryMoveDungeon(map: DungeonFloor, x: Float, y: Float): Boolean {
        if (!map.isWalkable(x, y)) return false
        // 몸통 반경 충돌
        val r = 14f
        if (!map.isWalkable(x - r, y) || !map.isWalkable(x + r, y) ||
            !map.isWalkable(x, y - r) || !map.isWalkable(x, y + r)
        ) return false
        dungeonHeroX = x
        dungeonHeroY = y
        return true
    }

    private fun updateDungeonHint(map: DungeonFloor) {
        dungeonHint = when (map.tileKindAt(dungeonHeroX, dungeonHeroY)) {
            DungeonTile.STAIRS_UP -> "stairs_up"
            DungeonTile.STAIRS_DOWN -> "stairs_down"
            else -> ""
        }
    }

    private fun wanderMonsters(map: DungeonFloor, dt: Float) {
        monsterWanderAcc += dt
        if (monsterWanderAcc < 0.45f) return
        monsterWanderAcc = 0f
        map.monsters.filter { it.alive }.forEach { zombie ->
            val ang = Random.nextFloat() * (Math.PI * 2).toFloat()
            val dist = Random.nextFloat() * 28f
            val nx = zombie.x + kotlin.math.cos(ang) * dist
            val ny = zombie.y + kotlin.math.sin(ang) * dist
            if (map.isWalkable(nx, ny)) {
                zombie.x = nx
                zombie.y = ny
            }
        }
        refreshDungeonFloor()
    }

    private fun checkDungeonContact(map: DungeonFloor) {
        if (dungeonCombatLock) return
        val hit = map.monsters.firstOrNull {
            it.alive && hypot(dungeonHeroX - it.x, dungeonHeroY - it.y) < DUNGEON_TOUCH_RANGE
        } ?: return
        dungeonTarget = null
        dungeonWalking = false
        fightDungeonMonster(hit)
    }

    private fun fightDungeonMonster(monster: DungeonMonster) {
        if (!monster.alive || dungeonCombatLock) return
        dungeonCombatLock = true
        emitSfx("hit")
        val floor = dungeonFloorNumber
        say("${monster.name}이(가) 생살 허기를 드러내며 덤벼든다!")

        val myPower = totalAtk + partyPower + blessBonus() + Random.nextInt(0, 10)
        val enemyPower = monster.power + Random.nextInt(0, 8)
        val dmg = (enemyPower - totalDef).coerceAtLeast(2) + Random.nextInt(0, 6)

        if (myPower >= enemyPower) {
            monster.alive = false
            refreshDungeonFloor()
            val gold = 18 + floor * 14 + Random.nextInt(0, 18)
            val exp = 20 + floor * 12
            val takenHit = (dmg / 2).coerceAtLeast(1)
            say("${monster.name}을(를) 쓰러뜨렸다! (HP -$takenHit, +${gold}G, EXP +$exp)")
            player = player.copy(gold = player.gold + gold)
            if (floor > player.dungeonDepth) {
                player = player.copy(dungeonDepth = floor)
            }
            if (applyDamage(takenHit)) return
            gainExp(exp)
            if (Random.nextInt(100) < 40) {
                val loot = ItemCatalog.dungeonLoot.random()
                addItem(loot)
                say("썩은 옷자락에서 ${loot.name}을(를) 챙겼다.")
            }
            if (mapCleared()) {
                say("이 층의 좀비가 잠잠해졌다. 심층의 계단을 찾아보자.")
            }
        } else {
            say("${monster.name}의 이빨이 스쳤다! (HP -$dmg)")
            if (applyDamage(dmg)) return
            gainExp(6)
            // 밀려남
            val push = 40f
            val ang = Random.nextFloat() * (Math.PI * 2).toFloat()
            tryMoveDungeon(
                dungeonFloor!!,
                dungeonHeroX + kotlin.math.cos(ang) * push,
                dungeonHeroY + kotlin.math.sin(ang) * push
            )
        }
        dungeonCombatLock = false
    }

    private fun mapCleared(): Boolean =
        dungeonFloor?.monsters?.none { it.alive } == true

    /** 구버전 UI 호환: 입구에서 바로 탐험을 시작할 때 사용 */
    fun exploreDungeon() {
        if (currentPlace != PlaceId.DUNGEON) {
            enterPlace(PlaceId.DUNGEON)
            return
        }
        say("화면을 눌러 통로를 걸어 다니며 좀비와 싸우자.")
    }

    private fun blessBonus(): Int = if (player.blessing > 0) 9 else 0

    /** @return 기절 여부 */
    private fun applyDamage(dmg: Int): Boolean {
        val hp = player.hp - dmg
        if (hp <= 0) {
            val lost = (player.gold * 0.2f).toInt()
            player = player.copy(hp = 1, gold = player.gold - lost)
            say("의식을 잃었다... 오염의 기운에 쓰러진 당신을 누군가가 병원으로 옮겼다. (${lost}G 분실)")
            // enterPlace는 로그를 지우므로, 기절 연출은 직접 병원으로 보낸다.
            clearDungeonState()
            pubTarget = null
            pendingPubNpc = null
            pubWalking = false
            currentPlace = PlaceId.HOSPITAL
            scene = Scene.INTERIOR
            menuTab = MenuTab.NONE
            say("눈을 떠보니 오염 상처를 돌보는 집 침대 위였다.")
            return true
        }
        player = player.copy(hp = hp)
        return false
    }

    // ---------------------------------------------------------------- 성장

    private fun gainExp(amount: Int) {
        var p = player.copy(exp = player.exp + amount)
        while (p.exp >= p.expToNext) {
            val rest = p.exp - p.expToNext
            p = p.copy(
                level = p.level + 1,
                exp = rest,
                maxHp = p.maxHp + 12,
                hp = p.maxHp + 12,
                maxMp = p.maxMp + 4,
                mp = p.maxMp + 4,
                baseAtk = p.baseAtk + 2,
                baseDef = p.baseDef + 1,
                str = p.str + 1,
                agi = p.agi + 1,
                intel = p.intel + 1
            )
            log.add("레벨 업! Lv.${p.level} 이(가) 되었다. 몸이 가벼워졌다.")
        }
        player = p
    }

    // ---------------------------------------------------------------- 표시용

    fun equippedSummary(): List<Pair<ItemType, EquippedItem?>> =
        EQUIP_SLOTS.map { it to equipment[it] }
}
