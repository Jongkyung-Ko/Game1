package com.medieval.village.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.medieval.village.model.DesertFactory
import com.medieval.village.model.DungeonFactory
import com.medieval.village.model.DungeonFloor
import com.medieval.village.model.DungeonMonster
import com.medieval.village.model.DungeonTile
import com.medieval.village.model.EQUIP_SLOTS
import com.medieval.village.model.EquippedItem
import com.medieval.village.model.ForestFactory
import com.medieval.village.model.GlacierFactory
import com.medieval.village.model.InteriorNpc
import com.medieval.village.model.InteriorNpcCatalog
import com.medieval.village.model.InteriorNpcKind
import com.medieval.village.model.InteriorRoom
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
import com.medieval.village.model.WeaponStyle
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class Facing { DOWN, UP, LEFT, RIGHT }

enum class Scene { VILLAGE, INTERIOR }

enum class MenuTab { NONE, STATUS, INVENTORY, EQUIPMENT, SYSTEM }

/** 도보 탐험 바이옴 */
enum class ExploreBiome { DUNGEON, FOREST, DESERT, GLACIER }

private data class Waypoint(val x: Float, val y: Float)

fun PlaceId?.exploreBiome(): ExploreBiome? = when (this) {
    PlaceId.DUNGEON -> ExploreBiome.DUNGEON
    PlaceId.EAST_FOREST -> ExploreBiome.FOREST
    PlaceId.SOUTH_DESERT -> ExploreBiome.DESERT
    PlaceId.NORTH_GLACIER -> ExploreBiome.GLACIER
    else -> null
}

/** 도보 탐험 지역(던전·숲·사막·빙하) 여부 */
fun PlaceId?.isExplorePlace(): Boolean = exploreBiome() != null

class GameViewModel : ViewModel() {

    companion object {
        private const val WALK_SPEED = 360f
        private const val DUNGEON_WALK_SPEED = 280f
        private const val DUNGEON_TOUCH_RANGE = 44f
        private const val MELEE_RANGE = 78f
        private const val MELEE_CONE_DOT = 0.35f // ~70° 전방
        private const val ATTACK_COOLDOWN = 0.42f
        private const val MAGIC_MP_COST = 6
        private const val PROJECTILE_SPEED = 420f
        private const val CONTACT_DAMAGE_INTERVAL = 0.75f
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
    /** 용병별 현재 HP (선두 피해용) */
    val mercHp = mutableStateMapOf<String, Int>()
    /**
     * 탐험 중 맨앞 캐릭터.
     * 0 = 주인공, 1+ = activeParty[index-1]
     */
    var frontIndex by mutableIntStateOf(0)
        private set

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
    /** 마을 동적 연출(새·동물·연기)용 누적 시간 */
    var animTime by mutableFloatStateOf(0f)
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

    /** 실내 NPC 말풍선 */
    var interiorSpeakerId by mutableStateOf<String?>(null)
        private set
    var interiorSpeech by mutableStateOf<String?>(null)
        private set
    private var interiorSpeechUntil = 0f

    /**
     * 실내(상점·집 등) 서비스/구매 패널 표시.
     * 주인(KEEPER)에게 다가가면 열린다.
     */
    var interiorPanelOpen by mutableStateOf(false)
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

    /** 던전 가상 패드 입력 (-1..1). 0이면 정지. */
    var dungeonPadX by mutableFloatStateOf(0f)
        private set
    var dungeonPadY by mutableFloatStateOf(0f)
        private set
    /** 근접 초승달 참격 연출 (폴백용 — 시트 애니메이션이 있으면 화면에서 생략) */
    var meleeSlashFx by mutableStateOf<MeleeSlashFx?>(null)
        private set
    /** 화살·마법 탄환 */
    val dungeonProjectiles = mutableStateListOf<DungeonProjectile>()
    /** 탄환/참격 프레임 갱신용 (Canvas 리컴포즈) */
    var dungeonCombatFrame by mutableIntStateOf(0)
        private set
    /** 공격 버튼 활성 여부 (쿨다운/마나) */
    var attackReady by mutableStateOf(true)
        private set
    /** 주인공 스프라이트 애니메이션 */
    var heroAnimKind by mutableStateOf(HeroAnimKind.IDLE)
        private set
    var heroAnimFrame by mutableIntStateOf(0)
        private set
    private var heroAnimTime = 0f
    private var attackAnimPlaying = false
    private val attackAnimDuration = 0.42f

    private val path = ArrayDeque<Waypoint>()
    private var pendingEnter: PlaceId? = null
    private var pubTarget: Waypoint? = null
    private var pendingPubNpc: PubNpc? = null
    private var interiorTarget: Waypoint? = null
    private var pendingInteriorNpc: InteriorNpc? = null
    private var dungeonTarget: Waypoint? = null
    private var pendingDungeonMonster: DungeonMonster? = null
    private var pendingChestCol: Int? = null
    private var pendingChestRow: Int? = null
    private var dungeonCombatLock = false
    private var monsterWanderAcc = 0f
    private var attackCooldown = 0f
    private var contactDamageAcc = 0f

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
        mercHp.clear()
        frontIndex = 0
        log.clear()
        path.clear()
        pendingEnter = null
        pubTarget = null
        pendingPubNpc = null
        interiorTarget = null
        pendingInteriorNpc = null
        interiorPanelOpen = false
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
        pubHeroX = InteriorRoom.SPAWN_X
        pubHeroY = InteriorRoom.SPAWN_Y
        pubWalking = false
        pubDialogue = null
        pubSpeakerId = null
        interiorPanelOpen = false
        scene = Scene.INTERIOR
        currentPlace = PlaceId.HOME
        menuTab = MenuTab.NONE
        say("풍요의 마을… 한때 '신성한 포도주'로 번영했던 이곳에 눈을 떴다.")
        say("몇 년 전 지하 최심부에서 검붉은 '좀비석'이 발굴된 뒤, 마을은 저주에 잠식되고 있다.")
        say("문을 열고, 지상으로 스며드는 재앙의 근원을 마주하자. 실내에서는 화면을 눌러 걸어 다닐 수 있다.")
    }

    // ---------------------------------------------------------------- 스탯 계산

    val equipAtk: Int get() = equipment.values.sumOf { it.atk }
    val equipDef: Int get() = equipment.values.sumOf { it.def }
    val skillPower: Int get() = skills.sumOf { it.power } / 2
    val activeParty: List<Mercenary>
        get() = activeMercenaryIds.mapNotNull { id -> party.firstOrNull { it.id == id } }
    val partyPower: Int get() = activeParty.sumOf { it.power }
    /** 주인공 + 원정대 */
    val partyActorCount: Int get() = 1 + activeParty.size

    val totalAtk: Int get() = player.baseAtk + equipAtk + player.str / 2 + skillPower
    val totalDef: Int get() = player.baseDef + equipDef + player.agi / 3

    fun frontMercenary(): Mercenary? =
        if (frontIndex <= 0) null else activeParty.getOrNull(frontIndex - 1)

    fun frontActorName(): String = frontMercenary()?.name ?: player.name

    fun mercCurrentHp(merc: Mercenary): Int = mercHp[merc.id] ?: merc.maxHp

    fun isMercAlive(merc: Mercenary): Boolean = mercCurrentHp(merc) > 0

    private fun ensureMercHp(merc: Mercenary) {
        if (merc.id !in mercHp) mercHp[merc.id] = merc.maxHp
    }

    private fun isActorAlive(index: Int): Boolean {
        if (index <= 0) return player.hp > 0
        val merc = activeParty.getOrNull(index - 1) ?: return false
        return isMercAlive(merc)
    }

    fun clampFrontIndex() {
        val n = partyActorCount
        if (n <= 0) {
            frontIndex = 0
            return
        }
        if (frontIndex !in 0 until n || !isActorAlive(frontIndex)) {
            frontIndex = 0
        }
    }

    /** 탐험 중 선두 교대 (쓰러진 용병은 건너뜀) */
    fun cyclePartyFront() {
        val n = partyActorCount
        if (n <= 1) {
            say("교대할 동료가 없다.")
            return
        }
        activeParty.forEach { ensureMercHp(it) }
        var next = (frontIndex + 1) % n
        repeat(n) {
            if (isActorAlive(next)) {
                frontIndex = next
                say("${frontActorName()}이(가) 맨앞으로 나섰다.")
                dungeonCombatFrame++
                return
            }
            next = (next + 1) % n
        }
        frontIndex = 0
        say("나설 수 있는 동료가 없다.")
    }

    // ---------------------------------------------------------------- 이동

    fun tick(dt: Float) {
        animTime += dt
        if (interiorSpeech != null && animTime >= interiorSpeechUntil) {
            interiorSpeech = null
            interiorSpeakerId = null
        }
        if (scene == Scene.INTERIOR && currentPlace == PlaceId.PUB) {
            tickPub(dt)
            return
        }
        if (scene == Scene.INTERIOR && currentPlace.isExplorePlace()) {
            tickDungeon(dt)
            return
        }
        if (scene == Scene.INTERIOR && currentPlace != null) {
            tickInteriorWalk(dt)
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

    /** 실내(상점·집 등)를 선술집처럼 걸어 다닌다. pubHero 좌표를 공유한다. */
    private fun tickInteriorWalk(dt: Float) {
        val target = interiorTarget
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
            interiorTarget = null
            pubWalking = false
            pendingInteriorNpc?.let { arriveInteriorNpc(it) }
            pendingInteriorNpc = null
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

    fun walkInInterior(x: Float, y: Float) {
        if (interiorPanelOpen) return
        interiorSpeech = null
        interiorSpeakerId = null
        pendingInteriorNpc = null
        interiorTarget = Waypoint(InteriorRoom.clampX(x), InteriorRoom.clampY(y))
    }

    fun approachInteriorNpc(npc: InteriorNpc) {
        if (interiorPanelOpen) return
        interiorSpeech = null
        interiorSpeakerId = null
        pendingInteriorNpc = npc
        interiorTarget = Waypoint(
            InteriorRoom.clampX(npc.worldX),
            InteriorRoom.clampY(npc.worldY + 70f),
        )
    }

    private fun arriveInteriorNpc(npc: InteriorNpc) {
        talkToInteriorNpc(npc.id)
        if (npc.kind == InteriorNpcKind.KEEPER) {
            interiorPanelOpen = true
            say("카운터에서 거래·서비스를 이용할 수 있다.")
        }
    }

    fun openInteriorPanel() {
        if (currentPlace == null || currentPlace.isExplorePlace() || currentPlace == PlaceId.PUB) return
        interiorPanelOpen = true
        interiorTarget = null
        pendingInteriorNpc = null
        pubWalking = false
    }

    fun closeInteriorPanel() {
        interiorPanelOpen = false
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

    /** 오크헤이븐 일러스트 맵에서는 목표점까지 직선으로 이동한다. */
    private fun buildPath(tx: Float, ty: Float) {
        path.clear()
        path.addLast(Waypoint(tx, ty))
    }

    // ---------------------------------------------------------------- 장소 출입

    fun enterPlace(id: PlaceId) {
        currentPlace = id
        scene = Scene.INTERIOR
        menuTab = MenuTab.NONE
        log.clear()
        interiorSpeech = null
        interiorSpeakerId = null
        interiorPanelOpen = false
        interiorTarget = null
        pendingInteriorNpc = null
        pubWalking = false
        if (id == PlaceId.PUB) {
            pubHeroX = 500f
            pubHeroY = 610f
            pubTarget = null
            pendingPubNpc = null
            pubDialogue = null
            pubSpeakerId = null
        } else if (!id.isExplorePlace()) {
            pubHeroX = InteriorRoom.SPAWN_X
            pubHeroY = InteriorRoom.SPAWN_Y
            pubTarget = null
            pendingPubNpc = null
            pubDialogue = null
            pubSpeakerId = null
        }
        if (id.isExplorePlace()) {
            enterExploreFloor(1)
        }
        emitSfx("door")
        greetInteriorNpcs(id)
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
        interiorTarget = null
        pendingInteriorNpc = null
        interiorPanelOpen = false
        walking = false
        pubWalking = false
        interiorSpeech = null
        interiorSpeakerId = null
        if (id.isExplorePlace()) clearDungeonState()
        scene = Scene.VILLAGE
        currentPlace = null
        menuTab = MenuTab.NONE
    }

    /** 실내 입장 시 NPC들이 번갈아 인사한다. */
    private fun greetInteriorNpcs(id: PlaceId) {
        val npcs = InteriorNpcCatalog.forPlace(id)
        if (npcs.isEmpty()) {
            say(greetingOf(id))
            return
        }
        val opener = npcs.first()
        val line = opener.lines.random()
        say("${opener.name}: $line")
        interiorSpeakerId = opener.id
        interiorSpeech = line
        interiorSpeechUntil = animTime + 3.2f
        if (npcs.size > 1) {
            val other = npcs.drop(1).random()
            say("${other.name}: ${other.lines.random()}")
        }
    }

    fun talkToInteriorNpc(npcId: String) {
        val npc = InteriorNpcCatalog.all.firstOrNull { it.id == npcId } ?: return
        val line = npc.lines.random()
        say("${npc.name}: $line")
        interiorSpeakerId = npc.id
        interiorSpeech = line
        interiorSpeechUntil = animTime + 3.0f
        emitSfx("click")
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
        PlaceId.EAST_FOREST -> "나뭇잎 사이로 바람이 스친다. 동쪽으로 갈수록 짐승의 울음이 가까워진다."
        PlaceId.SOUTH_DESERT -> "뜨거운 모래바람이 얼굴을 때린다. 전갈과 낙타거미가 모래 아래 숨는다."
        PlaceId.NORTH_GLACIER -> "칼바람과 함께 하얀 침묵이 내려앉는다. 북극의 짐승들이 얼음 너머에서 지켜본다."
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
        if (item.id == ItemCatalog.portalStone.id) {
            return usePortalStone()
        }
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

    /** 탐험 지역(던전·숲)에서만 사용 — 발밑에 집으로 가는 포털을 연다. */
    private fun usePortalStone(): Boolean {
        if (!currentPlace.isExplorePlace()) {
            say("포털스톤은 탐험 지역(던전·숲·사막·빙하)에서만 사용할 수 있다.")
            return false
        }
        val map = dungeonFloor
        if (map == null) {
            say("포털스톤은 탐험 지역(던전·숲·사막·빙하)에서만 사용할 수 있다.")
            return false
        }
        if (inventory.none { it.item.id == ItemCatalog.portalStone.id && it.count > 0 }) {
            say("포털스톤이 없다.")
            return false
        }
        val col = (dungeonHeroX / map.tileSize).toInt()
        val row = (dungeonHeroY / map.tileSize).toInt()
        val cell = map.tileAt(col, row)
        if (cell == DungeonTile.WALL ||
            cell == DungeonTile.STAIRS_UP ||
            cell == DungeonTile.STAIRS_DOWN ||
            cell == DungeonTile.VAULT ||
            cell == DungeonTile.CHEST_OPEN
        ) {
            say("여기에는 포털을 열 수 없다. 평평한 바닥 위에서 쓰자.")
            return false
        }
        // 층당 포털은 하나만 — 이전 포털은 바닥으로 되돌린다.
        map.clearPortals(DungeonTile.FLOOR)
        map.setTile(col, row, DungeonTile.PORTAL)
        removeItem(ItemCatalog.portalStone)
        dungeonHint = "portal"
        refreshDungeonFloor()
        emitSfx("door")
        say("포털스톤이 갈라지며 집으로 이어지는 푸른 문이 열렸다.")
        say("포털 위에서 ‘집으로’를 누르면 오두막으로 돌아간다. 탐험을 떠나면 문은 사라진다.")
        return true
    }

    private fun currentBiome(): ExploreBiome = currentPlace.exploreBiome() ?: ExploreBiome.DUNGEON

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
        // 카탈로그 템플릿을 복사해 성장·장비를 개별 관리한다.
        val hired = merc.copy(level = 1, exp = 0, equipment = emptyMap())
        party.add(hired)
        mercHp[hired.id] = hired.maxHp
        if (activeMercenaryIds.size < MAX_ACTIVE_MERCENARY) {
            activeMercenaryIds.add(merc.id)
            say("${merc.name}이(가) 동료가 되어 원정대에 합류했다! (-${merc.cost}G)")
        } else {
            say("${merc.name}을(를) 고용했다. Status에서 원정대를 편성할 수 있다. (-${merc.cost}G)")
        }
    }

    fun dismiss(merc: Mercenary) {
        val current = party.firstOrNull { it.id == merc.id } ?: return
        current.equipment.values.forEach { addItem(it.item) }
        party.removeAll { it.id == merc.id }
        activeMercenaryIds.remove(merc.id)
        mercHp.remove(merc.id)
        clampFrontIndex()
        say("${merc.name}과(와) 작별했다." + if (current.equipment.isNotEmpty()) " 착용 장비는 가방으로 돌아왔다." else "")
    }

    fun toggleMercenaryActive(merc: Mercenary) {
        if (merc.id in activeMercenaryIds) {
            activeMercenaryIds.remove(merc.id)
            clampFrontIndex()
            say("${merc.name}을(를) 원정대에서 대기시켰다.")
            return
        }
        if (activeMercenaryIds.size >= MAX_ACTIVE_MERCENARY) {
            say("원정대는 최대 ${MAX_ACTIVE_MERCENARY}명까지 선택할 수 있다.")
            return
        }
        activeMercenaryIds.add(merc.id)
        ensureMercHp(merc)
        say("${merc.name}이(가) 원정대에 합류했다.")
    }

    /** 용병에게 가방 장비를 장착한다. */
    fun equipMerc(mercId: String, item: Item) {
        if (!item.isEquipment) return
        val idx = party.indexOfFirst { it.id == mercId }
        if (idx < 0) {
            say("그 용병을 찾을 수 없다.")
            return
        }
        if (inventory.none { it.item.id == item.id && it.count > 0 }) {
            say("가방에 ${item.name}이(가) 없다.")
            return
        }
        val merc = party[idx]
        val prev = merc.equipment[item.type]
        removeItem(item)
        val nextGear = merc.equipment.toMutableMap()
        nextGear[item.type] = EquippedItem(item)
        if (prev != null) addItem(prev.item)
        party[idx] = merc.copy(equipment = nextGear)
        say(
            if (prev != null) {
                "${merc.name}에게 ${prev.displayName}을(를) 벗기고 ${item.name}을(를) 장착했다. (기여 +${party[idx].power})"
            } else {
                "${merc.name}에게 ${item.name}을(를) 장착했다. (기여 +${party[idx].power})"
            }
        )
    }

    fun unequipMerc(mercId: String, type: ItemType) {
        val idx = party.indexOfFirst { it.id == mercId }
        if (idx < 0) return
        val merc = party[idx]
        val cur = merc.equipment[type] ?: return
        val nextGear = merc.equipment.toMutableMap()
        nextGear.remove(type)
        party[idx] = merc.copy(equipment = nextGear)
        addItem(cur.item)
        say("${merc.name}의 ${cur.displayName}을(를) 가방으로 돌렸다.")
    }

    private fun gainMercExp(amount: Int) {
        if (amount <= 0) return
        val ids = activeMercenaryIds.toList()
        if (ids.isEmpty()) return
        ids.forEach { id ->
            val idx = party.indexOfFirst { it.id == id }
            if (idx < 0) return@forEach
            var m = party[idx].copy(exp = party[idx].exp + amount)
            var leveled = false
            while (m.exp >= m.expToNext) {
                val rest = m.exp - m.expToNext
                m = m.copy(
                    level = m.level + 1,
                    exp = rest,
                    basePower = m.basePower + 2,
                )
                leveled = true
                say("${m.name} 레벨 업! Lv.${m.level} · 전투 기여 +${m.power}")
            }
            party[idx] = m
            if (!leveled) {
                // 스팸 방지: 상세 로그는 생략, 전투 로그에만 합산 표기
            }
        }
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
        pendingChestCol = null
        pendingChestRow = null
        dungeonCombatLock = false
        monsterWanderAcc = 0f
        dungeonPadX = 0f
        dungeonPadY = 0f
        meleeSlashFx = null
        dungeonProjectiles.clear()
        attackCooldown = 0f
        contactDamageAcc = 0f
        dungeonCombatFrame = 0
        attackReady = true
        heroAnimKind = HeroAnimKind.IDLE
        heroAnimFrame = 0
        heroAnimTime = 0f
        attackAnimPlaying = false
        // frontIndex 는 마을·던전 공용 — 탈출 시에도 유지
    }

    fun setDungeonPad(dx: Float, dy: Float) {
        val len = hypot(dx, dy)
        if (len < 0.12f) {
            dungeonPadX = 0f
            dungeonPadY = 0f
            return
        }
        dungeonPadX = (dx / len).coerceIn(-1f, 1f)
        dungeonPadY = (dy / len).coerceIn(-1f, 1f)
        // 패드로 움직이면 자동 걷기 목표는 취소
        dungeonTarget = null
        pendingDungeonMonster = null
    }

    fun clearDungeonPad() {
        dungeonPadX = 0f
        dungeonPadY = 0f
    }

    /** 선두 캐릭터의 공격 방식 (주인공은 장착 무기, 용병은 역할) */
    fun currentWeaponStyle(): WeaponStyle {
        val merc = frontMercenary()
        if (merc != null) return merc.weaponStyle
        return equipment[ItemType.WEAPON]?.item?.weaponStyle ?: WeaponStyle.MELEE
    }

    fun attackLabel(): String = when (currentWeaponStyle()) {
        WeaponStyle.MELEE -> "휘두르기"
        WeaponStyle.BOW -> "발사"
        WeaponStyle.MAGIC -> "시전"
    }

    fun canDungeonAttack(): Boolean = attackReady && dungeonFloor != null && !dungeonCombatLock

    private fun refreshAttackReady() {
        val style = currentWeaponStyle()
        val needsHeroMp = style == WeaponStyle.MAGIC && frontMercenary() == null
        attackReady = attackCooldown <= 0f && (!needsHeroMp || player.mp >= MAGIC_MP_COST)
    }

    /** 공격 버튼 — 선두가 근접 참격 또는 화살/마법 발사 */
    fun dungeonAttack() {
        val map = dungeonFloor ?: return
        if (dungeonCombatLock || attackCooldown > 0f) return
        clampFrontIndex()
        val frontMerc = frontMercenary()
        if (frontMerc != null && !isMercAlive(frontMerc)) {
            frontIndex = 0
        }
        val style = currentWeaponStyle()
        val heroCasting = style == WeaponStyle.MAGIC && frontMercenary() == null
        if (heroCasting && player.mp < MAGIC_MP_COST) {
            say("마나가 부족하다.")
            refreshAttackReady()
            return
        }
        attackCooldown = ATTACK_COOLDOWN
        refreshAttackReady()
        val dmg = if (frontMercenary() != null) {
            val m = frontMercenary()!!
            (m.power + blessBonus() / 2 + Random.nextInt(0, 5)).coerceAtLeast(4)
        } else {
            (totalAtk + partyPower / 2 + blessBonus() / 2 + Random.nextInt(0, 5))
                .coerceAtLeast(4)
        }
        emitSfx("hit")
        startAttackAnim(
            when (style) {
                WeaponStyle.MELEE -> HeroAnimKind.SLASH
                WeaponStyle.BOW -> HeroAnimKind.BOW
                WeaponStyle.MAGIC -> HeroAnimKind.MAGIC
            }
        )
        when (style) {
            WeaponStyle.MELEE -> performMeleeSlash(map, dmg)
            WeaponStyle.BOW -> spawnProjectile(WeaponStyle.BOW, dmg, life = 1.15f)
            WeaponStyle.MAGIC -> {
                if (heroCasting) {
                    player = player.copy(mp = player.mp - MAGIC_MP_COST)
                }
                spawnProjectile(WeaponStyle.MAGIC, dmg + 3, life = 1.25f)
            }
        }
        refreshAttackReady()
    }

    private fun startAttackAnim(kind: HeroAnimKind) {
        heroAnimKind = kind
        heroAnimFrame = 0
        heroAnimTime = 0f
        attackAnimPlaying = true
        dungeonCombatFrame++
    }

    private fun tickHeroAnim(dt: Float, moving: Boolean) {
        if (attackAnimPlaying) {
            heroAnimTime += dt
            heroAnimFrame = ((heroAnimTime / attackAnimDuration) * 4f).toInt().coerceIn(0, 3)
            if (heroAnimTime >= attackAnimDuration) {
                attackAnimPlaying = false
                heroAnimKind = if (moving) HeroAnimKind.WALK else HeroAnimKind.IDLE
                heroAnimFrame = 0
                heroAnimTime = 0f
            }
            dungeonCombatFrame++
            return
        }
        if (moving) {
            heroAnimKind = HeroAnimKind.WALK
            heroAnimFrame = (((walkPhase * 2.2f).toInt() % 4) + 4) % 4
        } else {
            heroAnimKind = HeroAnimKind.IDLE
            heroAnimFrame = 0
        }
    }

    private fun performMeleeSlash(map: DungeonFloor, damage: Int) {
        val origin = slashFxOrigin()
        meleeSlashFx = MeleeSlashFx(origin.first, origin.second, facing)
        val fx = facing.dirX()
        val fy = facing.dirY()
        val hits = map.monsters.filter { monster ->
            if (!monster.alive) return@filter false
            val dx = monster.x - dungeonHeroX
            val dy = monster.y - dungeonHeroY
            val dist = hypot(dx, dy)
            if (dist > MELEE_RANGE || dist < 1f) return@filter false
            val dot = (dx * fx + dy * fy) / dist
            dot >= MELEE_CONE_DOT
        }
        if (hits.isEmpty()) {
            say("칼날이 허공을 가른다.")
            return
        }
        hits.forEach { damageMonster(it, damage) }
    }

    private fun slashFxOrigin(): Pair<Float, Float> =
        dungeonHeroX + facing.dirX() * 18f to dungeonHeroY + facing.dirY() * 10f - 28f

    private fun spawnProjectile(style: WeaponStyle, damage: Int, life: Float) {
        val fx = facing.dirX()
        val fy = facing.dirY()
        // 대각 패드 입력 중이면 그 방향으로도 보정
        val px = if (abs(dungeonPadX) + abs(dungeonPadY) > 0.2f) dungeonPadX else fx
        val py = if (abs(dungeonPadX) + abs(dungeonPadY) > 0.2f) dungeonPadY else fy
        val len = hypot(px, py).coerceAtLeast(0.01f)
        val nx = px / len
        val ny = py / len
        facing = if (abs(nx) > abs(ny)) {
            if (nx > 0) Facing.RIGHT else Facing.LEFT
        } else {
            if (ny > 0) Facing.DOWN else Facing.UP
        }
        dungeonProjectiles += DungeonProjectile(
            x = dungeonHeroX + nx * 22f,
            y = dungeonHeroY + ny * 10f - 30f,
            vx = nx * PROJECTILE_SPEED,
            vy = ny * PROJECTILE_SPEED,
            style = style,
            damage = damage,
            life = life,
        )
        say(if (style == WeaponStyle.BOW) "화살을 날렸다!" else "마력을 쏘아냈다!")
    }

    private fun damageMonster(monster: DungeonMonster, damage: Int) {
        if (!monster.alive) return
        monster.hp -= damage
        if (monster.hp > 0) {
            say("${monster.name}에게 ${damage} 피해! (HP ${monster.hp}/${monster.maxHp})")
            refreshDungeonFloor()
            return
        }
        monster.alive = false
        monster.hp = 0
        refreshDungeonFloor()
        rewardMonsterKill(monster)
    }

    private fun rewardMonsterKill(monster: DungeonMonster) {
        val floor = dungeonFloorNumber
        val biome = currentBiome()
        val wild = biome != ExploreBiome.DUNGEON
        val gold = (if (wild) 14 else 18) + floor * (if (wild) 11 else 14) + Random.nextInt(0, 16)
        val exp = (if (wild) 16 else 20) + floor * (if (wild) 10 else 12)
        say("${monster.name}을(를) 쓰러뜨렸다! (+${gold}G, EXP +$exp)")
        player = player.copy(gold = player.gold + gold)
        when (biome) {
            ExploreBiome.FOREST -> if (floor > player.forestDepth) player = player.copy(forestDepth = floor)
            ExploreBiome.DESERT -> if (floor > player.desertDepth) player = player.copy(desertDepth = floor)
            ExploreBiome.GLACIER -> if (floor > player.glacierDepth) player = player.copy(glacierDepth = floor)
            ExploreBiome.DUNGEON -> if (floor > player.dungeonDepth) player = player.copy(dungeonDepth = floor)
        }
        gainExp(exp)
        gainMercExp(exp)
        if (activeParty.isNotEmpty()) {
            say("원정대 용병도 경험을 쌓았다. (+${exp} EXP)")
        }
        if (Random.nextInt(100) < 40) {
            val loot = when (biome) {
                ExploreBiome.FOREST -> ItemCatalog.forestLoot.random()
                ExploreBiome.DESERT -> ItemCatalog.desertLoot.random()
                ExploreBiome.GLACIER -> ItemCatalog.glacierLoot.random()
                ExploreBiome.DUNGEON -> ItemCatalog.dungeonLoot.random()
            }
            addItem(loot)
            say(
                when (biome) {
                    ExploreBiome.FOREST -> "쓰러진 짐승 곁에서 ${loot.name}을(를) 챙겼다."
                    ExploreBiome.DESERT -> "모래 속에서 ${loot.name}을(를) 주웠다."
                    ExploreBiome.GLACIER -> "얼음 틈에서 ${loot.name}을(를) 챙겼다."
                    ExploreBiome.DUNGEON -> "썩은 옷자락에서 ${loot.name}을(를) 챙겼다."
                }
            )
        }
        if (mapCleared()) {
            say(
                when (biome) {
                    ExploreBiome.FOREST -> "이 지대의 짐승이 잠잠해졌다. 더 깊은 숲길을 찾아보자."
                    ExploreBiome.DESERT -> "이 지대의 괴물이 잠잠해졌다. 더 깊은 모래길을 찾아보자."
                    ExploreBiome.GLACIER -> "이 지대의 극지 짐승이 잠잠해졌다. 더 깊은 빙하를 찾아보자."
                    ExploreBiome.DUNGEON -> "이 층의 좀비가 잠잠해졌다. 심층의 계단을 찾아보자."
                }
            )
        }
    }

    /** 몬스터 위치/사망 등 내부 변이 후 Compose 재구성을 유도한다. */
    private fun refreshDungeonFloor() {
        val map = dungeonFloor ?: return
        dungeonFloor = map.copy(monsters = map.monsters.toMutableList())
    }

    private fun enterExploreFloor(floor: Int) {
        val biome = currentBiome()
        val map = when (biome) {
            ExploreBiome.FOREST -> ForestFactory.generate(floor)
            ExploreBiome.DESERT -> DesertFactory.generate(floor)
            ExploreBiome.GLACIER -> GlacierFactory.generate(floor)
            ExploreBiome.DUNGEON -> DungeonFactory.generate(floor)
        }
        dungeonFloor = map
        dungeonFloorNumber = floor
        dungeonHeroX = map.spawnX
        dungeonHeroY = map.spawnY
        dungeonWalking = false
        dungeonTarget = null
        pendingDungeonMonster = null
        pendingChestCol = null
        pendingChestRow = null
        dungeonCombatLock = false
        dungeonPadX = 0f
        dungeonPadY = 0f
        meleeSlashFx = null
        dungeonProjectiles.clear()
        attackCooldown = 0f
        contactDamageAcc = 0f
        attackReady = true
        heroAnimKind = HeroAnimKind.IDLE
        heroAnimFrame = 0
        heroAnimTime = 0f
        attackAnimPlaying = false
        // 층 진입 시 원정대 체력 회복
        activeParty.forEach { mercHp[it.id] = it.maxHp }
        clampFrontIndex()
        dungeonHint = "stairs_up"
        when (biome) {
            ExploreBiome.FOREST -> {
                if (floor > player.forestDepth) player = player.copy(forestDepth = floor)
                say("─── 동쪽 숲 · ${floor}지대 ───")
                say(
                    if (floor == 1) "마을 동쪽 숲길이 열린다. 토끼와 여우가 덤불 사이로 스친다."
                    else "숲이 더 짙어진다. 발밑 낙엽이 축축하고, 짐승의 숨결이 가까워진다."
                )
            }
            ExploreBiome.DESERT -> {
                if (floor > player.desertDepth) player = player.copy(desertDepth = floor)
                say("─── 남쪽 사막 · ${floor}지대 ───")
                say(
                    if (floor == 1) "모래언덕 사이로 길이 열린다. 전갈과 사막여우의 기척이 느껴진다."
                    else "모래바람이 거세진다. 낙타거미와 거대전갈이 더 깊은 모래 아래 숨는다."
                )
            }
            ExploreBiome.GLACIER -> {
                if (floor > player.glacierDepth) player = player.copy(glacierDepth = floor)
                say("─── 북쪽 빙하 · ${floor}지대 ───")
                say(
                    if (floor == 1) "하얀 빙판이 이어진다. 펭귄 떼와 눈여우가 얼음 사이를 누빈다."
                    else "칼바람이 살을 에는다. 북극곰과 설인의 발자국이 눈 위에 선명하다."
                )
            }
            ExploreBiome.DUNGEON -> {
                if (floor > player.dungeonDepth) player = player.copy(dungeonDepth = floor)
                say("─── 지하 ${floor}층 · 오염된 통로 ───")
                say(
                    if (floor == 1) "한때 포도주 보관소와 하수도였던 길이 좀비의 숨결로 가득하다."
                    else "더 깊은 곳에서 검붉은 기운이 피부를 찌른다. 좀비석이 가까워지는 기분이다."
                )
            }
        }
    }

    /** 탐험 화면 진입 시 맵이 없으면 즉시 생성한다. */
    fun ensureDungeonLoaded() {
        if (dungeonFloor == null) enterExploreFloor(1)
    }

    fun walkInDungeon(x: Float, y: Float) {
        val map = dungeonFloor ?: return
        if (dungeonCombatLock) return
        pendingDungeonMonster = null
        pendingChestCol = null
        pendingChestRow = null
        val tx = x.coerceIn(DungeonFactory.TILE, map.worldW - DungeonFactory.TILE)
        val ty = y.coerceIn(DungeonFactory.TILE, map.worldH - DungeonFactory.TILE)
        if (!map.isWalkable(tx, ty)) return
        dungeonTarget = Waypoint(tx, ty)
    }

    fun approachDungeonMonster(monster: DungeonMonster) {
        // 패드 전투로 전환 — 탭으로 자동 접근/전투하지 않는다.
        if (!monster.alive) return
        say("${monster.name} — 왼쪽 패드로 다가가 오른쪽 공격 버튼으로 맞서자.")
    }

    fun descendDungeon() {
        val map = dungeonFloor ?: return
        if (map.tileKindAt(dungeonHeroX, dungeonHeroY) != DungeonTile.STAIRS_DOWN) {
            say(
                when (currentBiome()) {
                    ExploreBiome.FOREST -> "더 깊은 숲길로 이어지는 표식 위에 서야 한다."
                    ExploreBiome.DESERT -> "더 깊은 사막길로 이어지는 표식 위에 서야 한다."
                    ExploreBiome.GLACIER -> "더 깊은 빙하로 이어지는 표식 위에 서야 한다."
                    ExploreBiome.DUNGEON -> "아래층으로 이어지는 계단 위에 서야 한다."
                }
            )
            return
        }
        if (player.hp <= player.maxHp * 0.15f) {
            say("몸 상태로는 더 들어갈 수 없다. 물약을 쓰거나 마을로 돌아가자.")
            return
        }
        emitSfx("door")
        enterExploreFloor(dungeonFloorNumber + 1)
    }

    fun escapeDungeon() {
        say(
            when (currentBiome()) {
                ExploreBiome.FOREST -> "마을 쪽 바람이 폐를 채운다. 숲속 짐승들은 여전히 깊은 곳에서 숨 쉰다."
                ExploreBiome.DESERT -> "마을 쪽 공기가 폐를 채운다. 모래 아래 괴물들은 여전히 숨 쉰다."
                ExploreBiome.GLACIER -> "마을 쪽 온기가 손을 녹인다. 극지의 짐승들은 여전히 얼음 너머에 있다."
                ExploreBiome.DUNGEON -> "지상의 공기가 폐를 채운다. 저주는 아직 지하에 웅크리고 있다."
            }
        )
        emitSfx("door")
        leavePlace()
    }

    /** 발밑(또는 지정 칸)의 닫힌 보물상자를 연다. */
    fun openDungeonChest(col: Int? = null, row: Int? = null) {
        val map = dungeonFloor ?: return
        val c = col ?: (dungeonHeroX / map.tileSize).toInt()
        val r = row ?: (dungeonHeroY / map.tileSize).toInt()
        if (map.tileAt(c, r) != DungeonTile.VAULT) {
            say("열 수 있는 보물상자가 없다.")
            return
        }
        val cx = c * map.tileSize + map.tileSize / 2f
        val cy = r * map.tileSize + map.tileSize / 2f
        if (hypot(dungeonHeroX - cx, dungeonHeroY - cy) > DungeonFactory.TILE * 0.85f) {
            pendingDungeonMonster = null
            pendingChestCol = c
            pendingChestRow = r
            dungeonTarget = Waypoint(cx, cy)
            say("보물상자로 다가간다…")
            return
        }
        pendingChestCol = null
        pendingChestRow = null
        map.setTile(c, r, DungeonTile.CHEST_OPEN)
        refreshDungeonFloor()
        emitSfx("click")
        val floor = dungeonFloorNumber
        val gold = 12 + floor * 10 + Random.nextInt(0, 16)
        player = player.copy(gold = player.gold + gold)
        val lootCount = if (Random.nextFloat() < 0.28f + floor * 0.04f) 2 else 1
        val found = mutableListOf<Item>()
        repeat(lootCount) {
            val item = rollChestLoot(floor)
            addItem(item)
            found += item
        }
        say("보물상자를 열었다! (+${gold}G)")
        found.forEach { say("상자 안에서 ${it.name}을(를) 얻었다.") }
        dungeonHint = "chest_open"
    }

    private fun rollChestLoot(floor: Int): Item {
        val base = when (currentBiome()) {
            ExploreBiome.FOREST -> ItemCatalog.forestLoot
            ExploreBiome.DESERT -> ItemCatalog.desertLoot
            ExploreBiome.GLACIER -> ItemCatalog.glacierLoot
            ExploreBiome.DUNGEON -> ItemCatalog.dungeonLoot
        }
        val deep = listOf(
            ItemCatalog.hiPotion,
            ItemCatalog.ironSword,
            ItemCatalog.ironShield,
            ItemCatalog.chainMail,
            ItemCatalog.manaAmulet,
            ItemCatalog.portalStone,
            ItemCatalog.luckyRing,
        )
        val pool = if (floor >= 3 && Random.nextFloat() < 0.42f) deep + base else base
        return pool.random()
    }

    /** 포털스톤으로 연 문을 타고 주인공 집(HOME)으로 귀환한다. */
    fun enterHomePortal() {
        val map = dungeonFloor ?: return
        if (map.tileKindAt(dungeonHeroX, dungeonHeroY) != DungeonTile.PORTAL) {
            say("집으로 이어지는 포털 위에 서야 한다.")
            return
        }
        say("포털이 빛나며 오두막으로 당신을 끌어당긴다.")
        emitSfx("door")
        clearDungeonState()
        path.clear()
        pendingEnter = null
        pubTarget = null
        pendingPubNpc = null
        walking = false
        pubWalking = false
        interiorSpeech = null
        interiorSpeakerId = null
        val home = Village.of(PlaceId.HOME)
        heroX = home.doorX
        heroY = home.doorY
        facing = Facing.DOWN
        currentPlace = PlaceId.HOME
        scene = Scene.INTERIOR
        menuTab = MenuTab.NONE
        say("익숙한 오두막의 공기가 폐를 채운다. 던전으로 돌아가면 그 포털은 이미 닫혀 있을 것이다.")
    }

    private fun tickDungeon(dt: Float) {
        val map = dungeonFloor ?: return
        updateDungeonHint(map)
        wanderMonsters(map, dt)
        tickAttackFx(dt)
        tickProjectiles(map, dt)
        applyContactThreat(map, dt)

        if (attackCooldown > 0f) {
            attackCooldown = (attackCooldown - dt).coerceAtLeast(0f)
            if (attackCooldown <= 0f) refreshAttackReady()
        }

        // 1) 가상 패드 우선 이동
        val padLen = hypot(dungeonPadX, dungeonPadY)
        if (padLen > 0.05f) {
            val step = DUNGEON_WALK_SPEED * dt
            val nx = dungeonHeroX + dungeonPadX * step
            val ny = dungeonHeroY + dungeonPadY * step
            val moved = tryMoveDungeon(map, nx, ny)
            if (!moved) {
                val movedX = tryMoveDungeon(map, nx, dungeonHeroY)
                val movedY = tryMoveDungeon(map, dungeonHeroX, ny)
                dungeonWalking = movedX || movedY
            } else {
                dungeonWalking = true
            }
            if (dungeonWalking) {
                walkPhase += dt * 10f
                if (!attackAnimPlaying) {
                    facing = if (abs(dungeonPadX) > abs(dungeonPadY)) {
                        if (dungeonPadX > 0) Facing.RIGHT else Facing.LEFT
                    } else {
                        if (dungeonPadY > 0) Facing.DOWN else Facing.UP
                    }
                }
            }
            tickHeroAnim(dt, moving = dungeonWalking)
            return
        }

        // 2) 보물상자 등 자동 접근 목표
        val target = dungeonTarget
        if (target == null) {
            dungeonWalking = false
            tickHeroAnim(dt, moving = false)
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
            pendingDungeonMonster = null
            val pc = pendingChestCol
            val pr = pendingChestRow
            if (pc != null && pr != null) {
                openDungeonChest(pc, pr)
            }
            tickHeroAnim(dt, moving = false)
            return
        }

        val nx = dungeonHeroX + dx / dist * step
        val ny = dungeonHeroY + dy / dist * step
        val moved = tryMoveDungeon(map, nx, ny)
        if (!moved) {
            val movedX = tryMoveDungeon(map, nx, dungeonHeroY)
            val movedY = tryMoveDungeon(map, dungeonHeroX, ny)
            if (!movedX && !movedY) {
                dungeonTarget = null
                dungeonWalking = false
                pendingDungeonMonster = null
                tickHeroAnim(dt, moving = false)
                return
            }
        }
        dungeonWalking = true
        walkPhase += dt * 10f
        if (!attackAnimPlaying) {
            facing = if (abs(dx) > abs(dy)) {
                if (dx > 0) Facing.RIGHT else Facing.LEFT
            } else {
                if (dy > 0) Facing.DOWN else Facing.UP
            }
        }
        tickHeroAnim(dt, moving = true)
    }

    private fun tickAttackFx(dt: Float) {
        val fx = meleeSlashFx ?: return
        fx.age += dt
        if (!fx.alive) meleeSlashFx = null
        else {
            meleeSlashFx = fx.copy(age = fx.age)
            dungeonCombatFrame++
        }
    }

    private fun tickProjectiles(map: DungeonFloor, dt: Float) {
        if (dungeonProjectiles.isEmpty()) return
        val doomed = mutableListOf<DungeonProjectile>()
        dungeonProjectiles.forEach { p ->
            p.life -= dt
            if (p.life <= 0f) {
                doomed += p
                return@forEach
            }
            val nx = p.x + p.vx * dt
            val ny = p.y + p.vy * dt
            if (!map.isWalkable(nx, ny)) {
                doomed += p
                return@forEach
            }
            p.x = nx
            p.y = ny
            val hit = map.monsters.firstOrNull {
                it.alive && hypot(it.x - p.x, it.y - p.y) < p.radius + 30f
            }
            if (hit != null) {
                damageMonster(hit, p.damage)
                doomed += p
            }
        }
        if (doomed.isNotEmpty()) {
            dungeonProjectiles.removeAll(doomed.toSet())
        }
        dungeonCombatFrame++
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
            DungeonTile.PORTAL -> "portal"
            DungeonTile.VAULT -> "chest"
            DungeonTile.CHEST_OPEN -> "chest_open"
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

    /** 적과 겹치면 주기적으로 피해 (자동 전투 대신 위협만 유지) */
    private fun applyContactThreat(map: DungeonFloor, dt: Float) {
        if (dungeonCombatLock) return
        val foe = map.monsters.firstOrNull {
            it.alive && hypot(dungeonHeroX - it.x, dungeonHeroY - it.y) < DUNGEON_TOUCH_RANGE
        }
        if (foe == null) {
            contactDamageAcc = 0f
            return
        }
        contactDamageAcc += dt
        if (contactDamageAcc < CONTACT_DAMAGE_INTERVAL) return
        contactDamageAcc = 0f
        val dmg = (foe.power - totalDef).coerceAtLeast(2) / 2 + Random.nextInt(0, 3)
        val biome = currentBiome()
        say(
            when (biome) {
                ExploreBiome.FOREST -> "${foe.name}의 발톱이 스친다! (HP -$dmg)"
                ExploreBiome.DESERT -> "${foe.name}의 독침이 스친다! (HP -$dmg)"
                ExploreBiome.GLACIER -> "${foe.name}의 한기가 스친다! (HP -$dmg)"
                ExploreBiome.DUNGEON -> "${foe.name}의 이빨이 스친다! (HP -$dmg)"
            }
        )
        applyFrontDamage(dmg)
        val push = 28f
        val ang = Random.nextFloat() * (Math.PI * 2).toFloat()
        tryMoveDungeon(
            map,
            dungeonHeroX + cos(ang) * push,
            dungeonHeroY + sin(ang) * push
        )
    }

    /** 맨앞 캐릭터가 피해를 받는다. 용병이 쓰러지면 주인공이 선두로. */
    private fun applyFrontDamage(dmg: Int): Boolean {
        clampFrontIndex()
        val merc = frontMercenary()
        if (merc == null) return applyDamage(dmg)
        ensureMercHp(merc)
        val hp = mercCurrentHp(merc) - dmg
        if (hp <= 0) {
            mercHp[merc.id] = 0
            say("${merc.name}이(가) 쓰러졌다! ${player.name}이(가) 앞으로 나선다.")
            frontIndex = 0
            dungeonCombatFrame++
            return false
        }
        mercHp[merc.id] = hp
        say("${merc.name} HP ${hp}/${merc.maxHp}")
        dungeonCombatFrame++
        return false
    }

    private fun mapCleared(): Boolean =
        dungeonFloor?.monsters?.none { it.alive } == true

    /** 구버전 UI 호환: 입구에서 바로 탐험을 시작할 때 사용 */
    fun exploreDungeon() {
        if (currentPlace != PlaceId.DUNGEON) {
            enterPlace(PlaceId.DUNGEON)
            return
        }
        say("왼쪽 패드로 이동하고, 오른쪽 버튼으로 공격하며 좀비와 싸우자.")
    }

    fun dungeonMoveHint(): String =
        "왼쪽 패드 이동 · 오른쪽 ${attackLabel()} · 상단 교대 · 상자 탭"

    fun frontStatusLabel(): String {
        val merc = frontMercenary()
        return if (merc != null) {
            "선두 ${merc.name} ${mercCurrentHp(merc)}/${merc.maxHp}"
        } else {
            "선두 ${player.name} ${player.hp}/${player.maxHp}"
        }
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
