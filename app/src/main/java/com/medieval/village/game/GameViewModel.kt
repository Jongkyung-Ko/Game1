package com.medieval.village.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.medieval.village.model.CastleFactory
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
import com.medieval.village.model.MercenaryCatalog
import com.medieval.village.model.Place
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Player
import com.medieval.village.model.PubNpc
import com.medieval.village.model.PubNpcCatalog
import com.medieval.village.model.RegionDialogue
import com.medieval.village.model.ActorClass
import com.medieval.village.model.Settlement
import com.medieval.village.model.SettlementId
import com.medieval.village.model.Settlements
import com.medieval.village.model.SkillMapOffer
import com.medieval.village.model.Skill
import com.medieval.village.model.SkillCatalog
import com.medieval.village.model.SkillSlotUi
import com.medieval.village.model.SpecialSkillCatalog
import com.medieval.village.model.SpecialSkillDef
import com.medieval.village.model.SpecialVfxSpec
import com.medieval.village.model.Village
import com.medieval.village.model.WeaponStyle
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

enum class Facing { DOWN, UP, LEFT, RIGHT }

enum class Scene { VILLAGE, INTERIOR }

enum class MenuTab { NONE, STATUS, INVENTORY, EQUIPMENT, SYSTEM, WORLD_MAP }

/** 도보 탐험 바이옴 */
enum class ExploreBiome { DUNGEON, FOREST, DESERT, GLACIER, CASTLE }

private data class Waypoint(val x: Float, val y: Float)

fun PlaceId?.exploreBiome(): ExploreBiome? = when (this) {
    PlaceId.DUNGEON -> ExploreBiome.DUNGEON
    PlaceId.EAST_FOREST -> ExploreBiome.FOREST
    PlaceId.SOUTH_DESERT -> ExploreBiome.DESERT
    PlaceId.NORTH_GLACIER -> ExploreBiome.GLACIER
    PlaceId.GRAY_CASTLE -> ExploreBiome.CASTLE
    else -> null
}

/** 도보 탐험 지역(던전·숲·사막·빙하) 여부 */
fun PlaceId?.isExplorePlace(): Boolean = exploreBiome() != null

class GameViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val WALK_SPEED = 360f
        private const val DUNGEON_WALK_SPEED = 280f
        private const val MELEE_RANGE = 78f
        private const val MELEE_CONE_DOT = 0.35f // ~70° 전방
        private const val ATTACK_COOLDOWN = 0.42f
        private const val SPECIAL_COOLDOWN = 2.15f
        private const val MAGIC_MP_COST = 6
        private const val PROJECTILE_SPEED = 420f
        private const val KNOCKBACK_DISTANCE = 42f
        private const val MONSTER_AGGRO_RANGE = 175f
        private const val MONSTER_ATTACK_RANGE = 52f
        private const val MONSTER_ATTACK_DURATION = 0.48f
        private const val MONSTER_ATTACK_COOLDOWN = 1.15f
        const val MAX_ACTIVE_MERCENARY = 2
        const val HERO_SKILL_KEY = "hero"
    }

    private val saveStore = GameSaveStore(application)

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

    /** actorKey("hero"/용병id) → 스킬id → 랭크(1..MAX). 없으면 미습득 */
    private val skillRanks = mutableStateMapOf<String, MutableMap<String, Int>>()
    /** actorKey → 남은 스킬포인트 */
    private val skillPoints = mutableStateMapOf<String, Int>()
    /** actorKey → 장착 슬롯 3개 (skill id or null) */
    private val specialSlots = mutableStateMapOf<String, List<String?>>()
    /** 레벨업·메뉴 스킬맵 UI */
    var levelUpSkillOffer by mutableStateOf<SkillMapOffer?>(null)
        private set
    /** 레벨업 특수 연출 (캐릭터 위 이펙트) */
    var levelUpFxActorKey by mutableStateOf<String?>(null)
        private set
    var levelUpFxUntil by mutableFloatStateOf(0f)
        private set
    /** 여러 캐릭터가 연속 레벨업할 때 대기열 */
    private val skillMapQueue = ArrayDeque<SkillMapOffer>()
    /** 특별스킬 쿨다운 (전투 HUD) */
    var specialReady by mutableStateOf(true)
        private set
    private var specialCooldown = 0f
    /** Compose 구독용 — 슬롯/랭크 변경 시 증가 */
    var specialSkillRevision by mutableIntStateOf(0)
        private set

    /** 현재 화면에서 보여줄 대사/결과 로그 */
    val log = mutableStateListOf<String>()

    var scene by mutableStateOf(Scene.INTERIOR)
        private set
    var currentPlace by mutableStateOf<PlaceId?>(PlaceId.HOME)
        private set
    var menuTab by mutableStateOf(MenuTab.NONE)

    /** 현재 머무는 정착지(마을) */
    var currentSettlement by mutableStateOf(SettlementId.OAKHAVEN)
        private set

    val settlement: Settlement get() = Settlements.of(currentSettlement, player.castleCleared)

    fun placeOf(id: PlaceId): Place =
        settlement.ofOrNull(id) ?: Settlements.oakhaven.of(id)

    var heroX by mutableFloatStateOf(Settlements.oakhaven.of(PlaceId.HOME).doorX)
        private set
    var heroY by mutableFloatStateOf(Settlements.oakhaven.of(PlaceId.HOME).doorY)
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
    /** 특별스킬 스프라이트 FX */
    val specialSkillFx = mutableStateListOf<SpecialSkillFx>()
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
    /** 특별스킬 캐릭터 애니 세트 (adv_smash 등). null이면 일반 slash/bow/magic */
    var specialAnimSet by mutableStateOf<String?>(null)
        private set
    private var heroAnimTime = 0f
    private var attackAnimPlaying = false
    private var attackAnimDuration = 0.42f

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
    /** 선두 이동 궤적 — 후열이 점프하지 않고 따라온다 */
    private val partyTrail = PartyTrail()

    init {
        newGame()
    }

    /** 현재 선두 기준 파티 그리기 슬롯 (궤적 추종 포함) */
    fun partyDrawSlots(leadX: Float, leadY: Float): List<PartyDrawSlot> {
        val line = PartyFormation.battleLine(frontIndex, activeParty)
        return line.mapIndexed { index, actor ->
            if (index == 0) {
                PartyDrawSlot(actor, leadX, leadY, facing, isFront = true)
            } else {
                val p = partyTrail.positionBehind(
                    distance = index * PartyFormation.SPACING,
                    leadX = leadX,
                    leadY = leadY,
                    leadFacing = facing,
                )
                PartyDrawSlot(actor, p.x, p.y, p.facing, isFront = false)
            }
        }
    }

    private fun noteLeaderMove(x: Float, y: Float) {
        partyTrail.record(x, y, facing)
    }

    private fun resetPartyTrail(x: Float, y: Float) {
        partyTrail.reset(x, y, facing)
    }

    // ---------------------------------------------------------------- 세이브/리셋

    fun saveSlotInfos(): List<SaveSlotInfo> = saveStore.allSlotInfo()

    /** Compose 갱신용 — 저장/로드/삭제 시 증가 */
    var saveRevision by mutableIntStateOf(0)
        private set

    fun saveGame(slot: Int): Boolean {
        val s = slot.coerceIn(1, GameSaveStore.SLOT_COUNT)
        return try {
            // 탐험 중이면 마을/실내로 정리해 안전하게 저장
            if (currentPlace.isExplorePlace()) {
                clearDungeonState()
                scene = Scene.VILLAGE
                currentPlace = null
            }
            saveStore.write(s, buildSaveJson())
            saveRevision++
            menuTab = MenuTab.NONE
            say("슬롯 $s 에 저장했다.")
            true
        } catch (t: Throwable) {
            say("저장에 실패했다.")
            false
        }
    }

    fun loadGame(slot: Int): Boolean {
        val s = slot.coerceIn(1, GameSaveStore.SLOT_COUNT)
        val json = saveStore.read(s) ?: run {
            say("슬롯 $s 에 저장된 데이터가 없다.")
            return false
        }
        return try {
            applySaveJson(json)
            saveRevision++
            menuTab = MenuTab.NONE
            say("슬롯 $s 에서 불러왔다.")
            true
        } catch (t: Throwable) {
            say("불러오기에 실패했다.")
            false
        }
    }

    fun deleteSave(slot: Int) {
        val s = slot.coerceIn(1, GameSaveStore.SLOT_COUNT)
        saveStore.clear(s)
        saveRevision++
        say("슬롯 $s 저장을 지웠다.")
    }

    private fun buildSaveJson(): JSONObject {
        val settlement = Settlements.of(currentSettlement, player.castleCleared)
        return JSONObject().apply {
            put("version", 1)
            put("savedAtMs", System.currentTimeMillis())
            put("settlementId", currentSettlement.name)
            put("settlementName", settlement.nameKo)
            put("scene", scene.name)
            put("currentPlace", currentPlace?.name)
            put("placeLabel", currentPlace?.let { placeOf(it).name } ?: "마을")
            put("heroX", heroX.toDouble())
            put("heroY", heroY.toDouble())
            put("facing", facing.name)
            put("pubHeroX", pubHeroX.toDouble())
            put("pubHeroY", pubHeroY.toDouble())
            put("arenaWins", arenaWins)
            put("arenaLosses", arenaLosses)
            put("frontIndex", frontIndex)
            put("player", JSONObject().apply {
                put("name", player.name)
                put("title", player.title)
                put("level", player.level)
                put("exp", player.exp)
                put("hp", player.hp)
                put("maxHp", player.maxHp)
                put("mp", player.mp)
                put("maxMp", player.maxMp)
                put("baseAtk", player.baseAtk)
                put("baseDef", player.baseDef)
                put("str", player.str)
                put("agi", player.agi)
                put("intel", player.intel)
                put("luck", player.luck)
                put("gold", player.gold)
                put("day", player.day)
                put("blessing", player.blessing)
                put("dungeonDepth", player.dungeonDepth)
                put("forestDepth", player.forestDepth)
                put("desertDepth", player.desertDepth)
                put("glacierDepth", player.glacierDepth)
                put("castleDepth", player.castleDepth)
                put("castleCleared", player.castleCleared)
            })
            put("inventory", JSONArray().apply {
                inventory.forEach { entry ->
                    put(JSONObject().apply {
                        put("id", entry.item.id)
                        put("count", entry.count)
                    })
                }
            })
            put("equipment", JSONObject().apply {
                equipment.forEach { (type, eq) ->
                    put(type.name, JSONObject().apply {
                        put("id", eq.item.id)
                        put("plus", eq.plus)
                    })
                }
            })
            put("skills", JSONArray().apply {
                skills.forEach { put(it.id) }
            })
            put("activeMercenaryIds", JSONArray().apply {
                activeMercenaryIds.forEach { put(it) }
            })
            put("party", JSONArray().apply {
                party.forEach { merc ->
                    put(JSONObject().apply {
                        put("id", merc.id)
                        put("level", merc.level)
                        put("exp", merc.exp)
                        put("hp", mercHp[merc.id] ?: merc.maxHp)
                        put("equipment", JSONObject().apply {
                            merc.equipment.forEach { (type, eq) ->
                                put(type.name, JSONObject().apply {
                                    put("id", eq.item.id)
                                    put("plus", eq.plus)
                                })
                            }
                        })
                    })
                }
            })
            put("skillPoints", JSONObject().apply {
                skillPoints.forEach { (k, v) -> put(k, v) }
            })
            put("skillRanks", JSONObject().apply {
                skillRanks.forEach { (actor, ranks) ->
                    put(actor, JSONObject().apply {
                        ranks.forEach { (sid, rank) -> put(sid, rank) }
                    })
                }
            })
            put("specialSlots", JSONObject().apply {
                specialSlots.forEach { (actor, slots) ->
                    put(actor, JSONArray().apply {
                        slots.forEach { id ->
                            if (id == null) put(JSONObject.NULL) else put(id)
                        }
                    })
                }
            })
        }
    }

    private fun applySaveJson(json: JSONObject) {
        clearDungeonState()
        path.clear()
        pendingEnter = null
        pubTarget = null
        pendingPubNpc = null
        interiorTarget = null
        pendingInteriorNpc = null
        interiorPanelOpen = false
        interiorSpeech = null
        interiorSpeakerId = null
        pubDialogue = null
        pubSpeakerId = null
        walking = false
        pubWalking = false
        levelUpSkillOffer = null
        skillMapQueue.clear()
        specialCooldown = 0f
        specialReady = true
        log.clear()

        val p = json.getJSONObject("player")
        player = Player(
            name = p.optString("name", "아서"),
            title = p.optString("title", "견습 모험가"),
            level = p.optInt("level", 1),
            exp = p.optInt("exp", 0),
            hp = p.optInt("hp", 60),
            maxHp = p.optInt("maxHp", 60),
            mp = p.optInt("mp", 20),
            maxMp = p.optInt("maxMp", 20),
            baseAtk = p.optInt("baseAtk", 8),
            baseDef = p.optInt("baseDef", 4),
            str = p.optInt("str", 8),
            agi = p.optInt("agi", 6),
            intel = p.optInt("intel", 5),
            luck = p.optInt("luck", 5),
            gold = p.optInt("gold", 300),
            day = p.optInt("day", 1),
            blessing = p.optInt("blessing", 0),
            dungeonDepth = p.optInt("dungeonDepth", 0),
            forestDepth = p.optInt("forestDepth", 0),
            desertDepth = p.optInt("desertDepth", 0),
            glacierDepth = p.optInt("glacierDepth", 0),
            castleDepth = p.optInt("castleDepth", 0),
            castleCleared = p.optBoolean("castleCleared", false),
        )

        inventory.clear()
        val inv = json.optJSONArray("inventory") ?: JSONArray()
        for (i in 0 until inv.length()) {
            val e = inv.getJSONObject(i)
            val item = ItemCatalog.byId(e.getString("id")) ?: continue
            inventory.add(InventoryEntry(item, e.optInt("count", 1).coerceAtLeast(1)))
        }

        equipment.clear()
        val eq = json.optJSONObject("equipment") ?: JSONObject()
        EQUIP_SLOTS.forEach { type ->
            val node = eq.optJSONObject(type.name) ?: return@forEach
            val item = ItemCatalog.byId(node.getString("id")) ?: return@forEach
            equipment[type] = EquippedItem(item, node.optInt("plus", 0))
        }

        skills.clear()
        val sk = json.optJSONArray("skills") ?: JSONArray()
        for (i in 0 until sk.length()) {
            SkillCatalog.byId(sk.getString(i))?.let { skills.add(it) }
        }

        party.clear()
        mercHp.clear()
        activeMercenaryIds.clear()
        val partyArr = json.optJSONArray("party") ?: JSONArray()
        for (i in 0 until partyArr.length()) {
            val node = partyArr.getJSONObject(i)
            val base = MercenaryCatalog.byId(node.getString("id")) ?: continue
            val mercEq = mutableMapOf<ItemType, EquippedItem>()
            val eqNode = node.optJSONObject("equipment") ?: JSONObject()
            EQUIP_SLOTS.forEach { type ->
                val e = eqNode.optJSONObject(type.name) ?: return@forEach
                val item = ItemCatalog.byId(e.getString("id")) ?: return@forEach
                mercEq[type] = EquippedItem(item, e.optInt("plus", 0))
            }
            val merc = base.copy(
                level = node.optInt("level", 1),
                exp = node.optInt("exp", 0),
                equipment = mercEq,
            )
            party.add(merc)
            mercHp[merc.id] = node.optInt("hp", merc.maxHp).coerceIn(1, merc.maxHp)
        }
        val act = json.optJSONArray("activeMercenaryIds") ?: JSONArray()
        for (i in 0 until act.length()) {
            val id = act.getString(i)
            if (party.any { it.id == id } && activeMercenaryIds.size < MAX_ACTIVE_MERCENARY) {
                activeMercenaryIds.add(id)
            }
        }

        skillRanks.clear()
        skillPoints.clear()
        specialSlots.clear()
        val sp = json.optJSONObject("skillPoints") ?: JSONObject()
        sp.keys().forEach { key -> skillPoints[key] = sp.optInt(key, 0) }
        val ranks = json.optJSONObject("skillRanks") ?: JSONObject()
        ranks.keys().forEach { actor ->
            val map = mutableMapOf<String, Int>()
            val node = ranks.getJSONObject(actor)
            node.keys().forEach { sid -> map[sid] = node.optInt(sid, 0) }
            skillRanks[actor] = map
        }
        val slots = json.optJSONObject("specialSlots") ?: JSONObject()
        slots.keys().forEach { actor ->
            val arr = slots.getJSONArray(actor)
            specialSlots[actor] = arr.toNullableStringList()
                .let { list ->
                    List(SpecialSkillCatalog.MAX_SLOTS) { i -> list.getOrNull(i) }
                }
        }
        ensureActorSkillState(HERO_SKILL_KEY)
        party.forEach { ensureActorSkillState(it.id) }
        specialSkillRevision++

        arenaWins = json.optInt("arenaWins", 0)
        arenaLosses = json.optInt("arenaLosses", 0)
        frontIndex = json.optInt("frontIndex", 0)
        clampFrontIndex()

        currentSettlement = runCatching {
            SettlementId.valueOf(json.optString("settlementId", SettlementId.OAKHAVEN.name))
        }.getOrDefault(SettlementId.OAKHAVEN)

        heroX = json.optDouble("heroX", placeOf(PlaceId.HOME).doorX.toDouble()).toFloat()
        heroY = json.optDouble("heroY", placeOf(PlaceId.HOME).doorY.toDouble()).toFloat()
        facing = runCatching {
            Facing.valueOf(json.optString("facing", Facing.DOWN.name))
        }.getOrDefault(Facing.DOWN)
        pubHeroX = json.optDouble("pubHeroX", InteriorRoom.SPAWN_X.toDouble()).toFloat()
        pubHeroY = json.optDouble("pubHeroY", InteriorRoom.SPAWN_Y.toDouble()).toFloat()

        val placeName = json.optString("currentPlace", "")
        val loadedPlace = placeName.takeIf { it.isNotEmpty() }?.let {
            runCatching { PlaceId.valueOf(it) }.getOrNull()
        }
        // 탐험 장소는 맵 상태가 없으므로 마을로 복귀
        if (loadedPlace != null && !loadedPlace.isExplorePlace()) {
            currentPlace = loadedPlace
            scene = Scene.INTERIOR
            resetPartyTrail(pubHeroX, pubHeroY)
        } else {
            currentPlace = null
            scene = Scene.VILLAGE
            resetPartyTrail(heroX, heroY)
        }
    }

    fun newGame() {
        player = Player()
        inventory.clear()
        equipment.clear()
        skills.clear()
        party.clear()
        activeMercenaryIds.clear()
        mercHp.clear()
        frontIndex = 0
        partyTrail.clear()
        skillRanks.clear()
        skillPoints.clear()
        specialSlots.clear()
        levelUpSkillOffer = null
        skillMapQueue.clear()
        specialCooldown = 0f
        specialReady = true
        specialSkillRevision = 0
        ensureActorSkillState(HERO_SKILL_KEY)
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

        currentSettlement = SettlementId.OAKHAVEN
        val home = placeOf(PlaceId.HOME)
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
        resetPartyTrail(pubHeroX, pubHeroY)
        say("풍요의 마을… 한때 '신성한 포도주'로 번영했던 이곳에 눈을 떴다.")
        say("몇 년 전 지하 최심부에서 검붉은 '좀비석'이 발굴된 뒤, 마을은 저주에 잠식되고 있다.")
        say("문을 열고, 지상으로 스며드는 재앙의 근원을 마주하자. 실내에서는 화면을 눌러 걸어 다닐 수 있다.")
    }

    /** 세계지도에서 정착지로 이동한다. */
    fun travelToSettlement(id: SettlementId) {
        if (currentPlace.isExplorePlace()) clearDungeonState()
        path.clear()
        pendingEnter = null
        pubTarget = null
        pendingPubNpc = null
        interiorTarget = null
        pendingInteriorNpc = null
        interiorPanelOpen = false
        interiorSpeech = null
        interiorSpeakerId = null
        walking = false
        pubWalking = false
        val moved = currentSettlement != id
        currentSettlement = id
        val home = placeOf(PlaceId.HOME)
        heroX = home.doorX
        heroY = home.doorY
        facing = Facing.DOWN
        scene = Scene.VILLAGE
        currentPlace = null
        menuTab = MenuTab.NONE
        resetPartyTrail(heroX, heroY)
        emitSfx("door")
        val s = Settlements.of(id, player.castleCleared)
        if (moved) {
            say("${s.nameKo}(${s.nameEn})에 도착했다. ${s.blurb}")
        } else {
            say("${s.nameKo} 지도를 펼쳤다.")
        }
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
                val leadX = if (currentPlace.isExplorePlace()) dungeonHeroX else
                    if (scene == Scene.INTERIOR) pubHeroX else heroX
                val leadY = if (currentPlace.isExplorePlace()) dungeonHeroY else
                    if (scene == Scene.INTERIOR) pubHeroY else heroY
                resetPartyTrail(leadX, leadY)
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
        if (levelUpFxActorKey != null && animTime >= levelUpFxUntil) {
            levelUpFxActorKey = null
        }
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
        noteLeaderMove(heroX, heroY)
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
            noteLeaderMove(pubHeroX, pubHeroY)
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
            noteLeaderMove(pubHeroX, pubHeroY)
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
        } else {
            resetPartyTrail(pubHeroX, pubHeroY)
        }
        emitSfx("door")
        greetInteriorNpcs(id)
    }

    fun leavePlace() {
        val id = currentPlace ?: return
        val place = placeOf(id)
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
        resetPartyTrail(heroX, heroY)
    }

    /** 실내 입장 시 NPC들이 번갈아 인사한다. */
    private fun greetInteriorNpcs(id: PlaceId) {
        val npcs = InteriorNpcCatalog.forPlace(id, currentSettlement, player.castleCleared)
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
        val npc = InteriorNpcCatalog.forPlace(
            currentPlace ?: return,
            currentSettlement,
            player.castleCleared,
        ).firstOrNull { it.id == npcId }
            ?: InteriorNpcCatalog.all.firstOrNull { it.id == npcId }
            ?: return
        val line = npc.lines.random()
        say("${npc.name}: $line")
        interiorSpeakerId = npc.id
        interiorSpeech = line
        interiorSpeechUntil = animTime + 3.0f
        emitSfx("click")
    }

    private fun greetingOf(id: PlaceId): String {
        RegionDialogue.placeGreeting(currentSettlement, player.castleCleared, id)?.let { return it }
        return when (id) {
            PlaceId.HOME -> "창문 너머로도 하수구 냄새가 스며든다. 그래도 여기는 나의 오두막이다."
            PlaceId.SHOP -> "\"어서 오세요… 횃불이랑 붕대는 늘 비치해 둡니다. 요즘엔 필수죠.\""
            PlaceId.WEAPON_SHOP -> "\"좀비 뼈라도 가를 쇠를 찾나? 잘 왔네.\""
            PlaceId.HOSPITAL -> "\"물린 상처입니까, 아니면… 좀비석 기운입니까?\""
            PlaceId.CHURCH -> "\"저주가 지상을 핥고 있소. 빛의 가호가 그대와 함께하기를.\""
            PlaceId.INN -> "\"문은 꼭 잠그세요. 밤엔 하수도 쪽에서 기척이 들립니다.\""
            PlaceId.PUB -> "포도주 향 사이로, 좀비석과 영주를 향한 낮은 원성이 섞여 들린다."
            PlaceId.ARENA -> "\"지상에서라도 칼날을 갈아야지. 지하에선 실수가 곧 죽음이야.\""
            PlaceId.DUNGEON -> "축축한 하수도 바람이 얼굴을 스친다. 저주의 둥지가 발밑에서 숨 쉰다."
            PlaceId.GRAY_CASTLE -> "회색 돌문이 열린다. 해골과 유령의 숨결이 성채 심층에서 흘러나온다."
            PlaceId.EAST_FOREST -> "나뭇잎 사이로 바람이 스친다. 동쪽으로 갈수록 짐승의 울음이 가까워진다."
            PlaceId.SOUTH_DESERT -> "뜨거운 모래바람이 얼굴을 때린다. 전갈과 낙타거미가 모래 아래 숨는다."
            PlaceId.NORTH_GLACIER -> "칼바람과 함께 하얀 침묵이 내려앉는다. 북극의 짐승들이 얼음 너머에서 지켜본다."
            PlaceId.BLACKSMITH -> "\"좀비 이빨에 안 깨지려면, 쇠는 더 두들겨야지.\""
            PlaceId.MAGIC_SCHOOL -> "\"연금술사들이 손을 댄 그 돌… 우리는 이제 해독만 연구한다네.\""
            PlaceId.MERCENARY -> "\"좀비 둥지 안내라면 돈만 주면 붙여주지. 목숨값은 별도야.\""
        }
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
        ensureActorSkillState(hired.id)
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
        skillRanks.remove(merc.id)
        skillPoints.remove(merc.id)
        specialSlots.remove(merc.id)
        specialSkillRevision++
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
            val levelsGained = mutableListOf<Int>()
            while (m.exp >= m.expToNext) {
                val rest = m.exp - m.expToNext
                m = m.copy(
                    level = m.level + 1,
                    exp = rest,
                    basePower = m.basePower + 2,
                )
                levelsGained += m.level
                say("${m.name} 레벨 업! Lv.${m.level} · 전투 기여 +${m.power}")
            }
            party[idx] = m
            if (levelsGained.isNotEmpty()) {
                onActorLevelUp(id, m.name, SpecialSkillCatalog.actorClassOf(m), levelsGained)
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
        specialSkillFx.clear()
        specialAnimSet = null
        attackAnimDuration = 0.42f
        dungeonProjectiles.clear()
        attackCooldown = 0f
        specialCooldown = 0f
        dungeonCombatFrame = 0
        attackReady = true
        specialReady = true
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
        startAttackAnim(
            when (style) {
                WeaponStyle.MELEE -> HeroAnimKind.SLASH
                WeaponStyle.BOW -> HeroAnimKind.BOW
                WeaponStyle.MAGIC -> HeroAnimKind.MAGIC
            }
        )
        when (style) {
            WeaponStyle.MELEE -> {
                emitSfx("hit") // 휘두르는 순간
                performMeleeSlash(map, dmg)
            }
            WeaponStyle.BOW -> {
                emitSfx("click")
                spawnProjectile(WeaponStyle.BOW, dmg, life = 1.15f)
            }
            WeaponStyle.MAGIC -> {
                if (heroCasting) {
                    player = player.copy(mp = player.mp - MAGIC_MP_COST)
                }
                emitSfx("click")
                spawnProjectile(WeaponStyle.MAGIC, dmg + 3, life = 1.25f)
            }
        }
        refreshAttackReady()
    }

    // ---------------------------------------------------------------- 특별스킬 / 스킬맵

    private fun ensureActorSkillState(actorKey: String) {
        if (skillRanks[actorKey] == null) skillRanks[actorKey] = mutableMapOf()
        if (skillPoints[actorKey] == null) skillPoints[actorKey] = 0
        if (specialSlots[actorKey] == null) {
            specialSlots[actorKey] = List(SpecialSkillCatalog.MAX_SLOTS) { null }
        }
    }

    private fun frontActorKey(): String = frontMercenary()?.id ?: HERO_SKILL_KEY

    private fun frontActorClass(): ActorClass = SpecialSkillCatalog.actorClassOf(frontMercenary())

    private fun actorLevelOf(actorKey: String): Int {
        if (actorKey == HERO_SKILL_KEY) return player.level
        return party.firstOrNull { it.id == actorKey }?.level ?: 1
    }

    private fun actorClassOfKey(actorKey: String): ActorClass {
        if (actorKey == HERO_SKILL_KEY) return ActorClass.ADVENTURER
        return SpecialSkillCatalog.actorClassOf(party.firstOrNull { it.id == actorKey })
    }

    fun skillPointsOf(actorKey: String): Int {
        ensureActorSkillState(actorKey)
        return skillPoints[actorKey] ?: 0
    }

    fun skillRankOf(actorKey: String, skillId: String): Int {
        ensureActorSkillState(actorKey)
        return skillRanks[actorKey]?.get(skillId) ?: 0
    }

    fun knownSpecialSkills(actorKey: String): List<SpecialSkillDef> {
        ensureActorSkillState(actorKey)
        val ranks = skillRanks[actorKey].orEmpty()
        return ranks.filterValues { it > 0 }.keys
            .mapNotNull { SpecialSkillCatalog.byId(it) }
            .sortedWith(compareBy({ it.mapCol }, { it.mapRow }))
    }

    fun slottedSpecialIds(actorKey: String): List<String?> {
        ensureActorSkillState(actorKey)
        return specialSlots[actorKey] ?: List(SpecialSkillCatalog.MAX_SLOTS) { null }
    }

    fun frontSkillSlotsUi(): List<SkillSlotUi> {
        @Suppress("UNUSED_EXPRESSION")
        specialSkillRevision
        val key = frontActorKey()
        ensureActorSkillState(key)
        val slots = slottedSpecialIds(key)
        return slots.mapIndexed { index, id ->
            val def = id?.let { SpecialSkillCatalog.byId(it) }
            val rank = if (id != null) skillRankOf(key, id) else 0
            val mp = if (def != null) SpecialSkillCatalog.mpCostAt(def, rank.coerceAtLeast(1)) else 0
            val mpOk = def == null || player.mp >= mp
            SkillSlotUi(
                slotIndex = index,
                skillId = id,
                shortName = when {
                    def == null -> "—"
                    rank > 1 -> "${def.shortName}$rank"
                    else -> def.shortName
                },
                enabled = def != null &&
                    specialReady &&
                    attackCooldown <= 0f &&
                    dungeonFloor != null &&
                    !dungeonCombatLock &&
                    mpOk &&
                    levelUpSkillOffer == null,
                mpCost = mp,
                rank = rank,
            )
        }
    }

    /** 선행·레벨·직군 조건을 만족하면 true (포인트는 별도 검사) */
    fun canUnlockSkill(actorKey: String, skillId: String): Boolean {
        ensureActorSkillState(actorKey)
        val def = SpecialSkillCatalog.byId(skillId) ?: return false
        if (def.actorClass != actorClassOfKey(actorKey)) return false
        if (skillRankOf(actorKey, skillId) > 0) return false
        if (actorLevelOf(actorKey) < def.unlockLevel) return false
        return def.requires.all { skillRankOf(actorKey, it) > 0 }
    }

    fun canRankUpSkill(actorKey: String, skillId: String): Boolean {
        val def = SpecialSkillCatalog.byId(skillId) ?: return false
        if (def.actorClass != actorClassOfKey(actorKey)) return false
        val rank = skillRankOf(actorKey, skillId)
        return rank in 1 until def.maxRank
    }

    fun learnSpecialSkill(actorKey: String, skillId: String): Boolean {
        ensureActorSkillState(actorKey)
        val def = SpecialSkillCatalog.byId(skillId) ?: return false
        if (!canUnlockSkill(actorKey, skillId)) return false
        val pts = skillPoints[actorKey] ?: 0
        if (pts < def.learnCost) {
            say("스킬포인트가 부족하다. (필요 ${def.learnCost})")
            return false
        }
        skillPoints[actorKey] = pts - def.learnCost
        skillRanks.getOrPut(actorKey) { mutableMapOf() }[skillId] = 1
        // 빈 슬롯에 자동 장착
        val slots = specialSlots[actorKey].orEmpty().toMutableList()
        while (slots.size < SpecialSkillCatalog.MAX_SLOTS) slots += null
        val empty = slots.indexOfFirst { it == null }
        if (empty >= 0 && skillId !in slots) {
            slots[empty] = skillId
            specialSlots[actorKey] = slots.toList()
        }
        specialSkillRevision++
        say("『${def.name}』을(를) 배웠다! (Lv.1)")
        return true
    }

    fun rankUpSpecialSkill(actorKey: String, skillId: String): Boolean {
        ensureActorSkillState(actorKey)
        val def = SpecialSkillCatalog.byId(skillId) ?: return false
        if (!canRankUpSkill(actorKey, skillId)) return false
        val pts = skillPoints[actorKey] ?: 0
        if (pts < def.rankUpCost) {
            say("스킬포인트가 부족하다. (필요 ${def.rankUpCost})")
            return false
        }
        val next = skillRankOf(actorKey, skillId) + 1
        skillPoints[actorKey] = pts - def.rankUpCost
        skillRanks.getOrPut(actorKey) { mutableMapOf() }[skillId] = next
        specialSkillRevision++
        val mult = SpecialSkillCatalog.damageMultAt(def, next)
        say("『${def.name}』 랭크 업! Lv.$next · ×${"%.1f".format(mult)}")
        return true
    }

    private fun onActorLevelUp(
        actorKey: String,
        actorName: String,
        cls: ActorClass,
        levelsGained: List<Int>,
    ) {
        if (levelsGained.isEmpty()) return
        val newLevel = levelsGained.last()
        ensureActorSkillState(actorKey)
        val gained = levelsGained.size
        skillPoints[actorKey] = (skillPoints[actorKey] ?: 0) + gained
        specialSkillRevision++
        emitSfx("level_up")
        levelUpFxActorKey = actorKey
        levelUpFxUntil = animTime + 2.0f
        say("$actorName 스킬포인트 +$gained (보유 ${skillPoints[actorKey]})")
        openSkillMap(
            actorKey = actorKey,
            actorName = actorName,
            actorClass = cls,
            actorLevel = newLevel,
            pointsGranted = gained,
            fromLevelUp = true,
        )
    }

    fun openSkillMap(
        actorKey: String,
        actorName: String = if (actorKey == HERO_SKILL_KEY) player.name
        else party.firstOrNull { it.id == actorKey }?.name ?: actorKey,
        actorClass: ActorClass = actorClassOfKey(actorKey),
        actorLevel: Int = actorLevelOf(actorKey),
        pointsGranted: Int = 0,
        fromLevelUp: Boolean = false,
    ) {
        ensureActorSkillState(actorKey)
        val offer = SkillMapOffer(
            actorKey = actorKey,
            actorName = actorName,
            actorClass = actorClass,
            actorLevel = actorLevel,
            pointsGranted = pointsGranted,
            fromLevelUp = fromLevelUp,
        )
        if (levelUpSkillOffer == null) {
            levelUpSkillOffer = offer
        } else if (
            levelUpSkillOffer?.actorKey == actorKey &&
            levelUpSkillOffer?.fromLevelUp == true &&
            fromLevelUp
        ) {
            // 같은 캐릭터 연속 레벨업이면 최신 레벨로 갱신·SP는 이미 합산됨
            levelUpSkillOffer = offer.copy(
                pointsGranted = (levelUpSkillOffer?.pointsGranted ?: 0) + pointsGranted,
            )
        } else if (fromLevelUp) {
            skillMapQueue.addLast(offer)
        } else {
            // 메뉴에서 연 경우 현재 세션을 덮어씀
            levelUpSkillOffer = offer
        }
        specialSkillRevision++
        if (fromLevelUp) {
            say("스킬맵에서 배울 스킬·강화할 스킬을 고르세요.")
        }
    }

    fun setSpecialSlot(actorKey: String, slotIndex: Int, skillId: String?) {
        ensureActorSkillState(actorKey)
        if (slotIndex !in 0 until SpecialSkillCatalog.MAX_SLOTS) return
        if (skillId != null) {
            if (skillRankOf(actorKey, skillId) <= 0) return
            val def = SpecialSkillCatalog.byId(skillId) ?: return
            if (def.actorClass != actorClassOfKey(actorKey)) return
        }
        val slots = specialSlots[actorKey].orEmpty().toMutableList()
        while (slots.size < SpecialSkillCatalog.MAX_SLOTS) slots += null
        if (skillId != null) {
            for (i in slots.indices) if (slots[i] == skillId) slots[i] = null
        }
        slots[slotIndex] = skillId
        specialSlots[actorKey] = slots.toList()
        specialSkillRevision++
    }

    fun clearSpecialSlot(actorKey: String, slotIndex: Int) {
        setSpecialSlot(actorKey, slotIndex, null)
    }

    fun dismissLevelUpSkillOffer() {
        levelUpSkillOffer = skillMapQueue.removeFirstOrNull()
    }

    fun confirmLevelUpSkillOffer() {
        val offer = levelUpSkillOffer ?: return
        val filled = slottedSpecialIds(offer.actorKey).count { it != null }
        val learned = knownSpecialSkills(offer.actorKey).size
        say("${offer.actorName} 스킬맵 완료 · 습득 ${learned} · 장착 ${filled}/${SpecialSkillCatalog.MAX_SLOTS} · SP ${skillPointsOf(offer.actorKey)}")
        levelUpSkillOffer = skillMapQueue.removeFirstOrNull()
    }

    /** 탐험 중 특별스킬 — 랭크에 따라 피해 상승 */
    fun dungeonSpecialAttack(slotIndex: Int) {
        val map = dungeonFloor ?: return
        if (dungeonCombatLock || attackCooldown > 0f || specialCooldown > 0f) return
        if (levelUpSkillOffer != null) return
        clampFrontIndex()
        val frontMerc = frontMercenary()
        if (frontMerc != null && !isMercAlive(frontMerc)) {
            frontIndex = 0
        }
        val key = frontActorKey()
        ensureActorSkillState(key)
        val skillId = slottedSpecialIds(key).getOrNull(slotIndex) ?: return
        val skill = SpecialSkillCatalog.byId(skillId) ?: return
        if (skill.actorClass != frontActorClass()) {
            say("지금 선두 직업으로는 쓸 수 없는 스킬이다.")
            return
        }
        val rank = skillRankOf(key, skillId).coerceAtLeast(1)
        val mpCost = SpecialSkillCatalog.mpCostAt(skill, rank)
        if (player.mp < mpCost) {
            say("마나가 부족하다. (MP $mpCost)")
            return
        }
        player = player.copy(mp = player.mp - mpCost)
        attackCooldown = ATTACK_COOLDOWN
        specialCooldown = SPECIAL_COOLDOWN
        specialReady = false
        refreshAttackReady()
        specialSkillRevision++

        val base = if (frontMercenary() != null) {
            val m = frontMercenary()!!
            (m.power + blessBonus() / 2 + Random.nextInt(0, 5)).coerceAtLeast(4)
        } else {
            (totalAtk + partyPower / 2 + blessBonus() / 2 + Random.nextInt(0, 5)).coerceAtLeast(4)
        }
        val mult = SpecialSkillCatalog.damageMultAt(skill, rank)
        val dmg = (base * mult).roundToInt().coerceAtLeast(base + 8)
        say("『${skill.name}』 Lv.$rank! (×${"%.1f".format(mult)} · 피해 $dmg)")
        val vfx = SpecialSkillCatalog.vfxFor(skill.id)
        emitSfx(SpecialSkillCatalog.sfxKeyFor(skill.id))
        val animKind = when (skill.style) {
            WeaponStyle.MELEE -> HeroAnimKind.SLASH
            WeaponStyle.BOW -> HeroAnimKind.BOW
            WeaponStyle.MAGIC -> HeroAnimKind.MAGIC
        }
        val heroFront = frontMercenary() == null
        startAttackAnim(
            kind = animKind,
            duration = vfx?.animDuration ?: 0.52f,
            specialSet = if (heroFront) vfx?.animSet else null,
        )
        if (heroFront && vfx != null) {
            spawnSpecialMeleeFx(vfx)
        }
        when (skill.style) {
            WeaponStyle.MELEE -> {
                if (skill.id == "adv_charge" && heroFront) {
                    lungeForward(map, 36f)
                }
                performMeleeSlash(
                    map = map,
                    damage = dmg,
                    emitCrescent = vfx?.meleeFxKey == null,
                    slashPower = if (vfx?.meleeFxKey == null) 1.45f else 1f,
                    slashDuration = if (vfx?.meleeFxKey == null) 0.48f else 0.34f,
                )
            }
            WeaponStyle.BOW -> {
                spawnProjectile(
                    style = WeaponStyle.BOW,
                    damage = dmg,
                    life = 1.25f,
                    fxSpriteKey = vfx?.projectileFxKey,
                    impactSpriteKey = vfx?.impactFxKey,
                    radius = if (vfx?.projectileFxKey != null) 22f else 18f,
                )
            }
            WeaponStyle.MAGIC -> {
                spawnProjectile(
                    style = WeaponStyle.MAGIC,
                    damage = dmg + 4,
                    life = 1.35f,
                    fxSpriteKey = vfx?.projectileFxKey,
                    impactSpriteKey = vfx?.impactFxKey,
                    radius = if (vfx?.projectileFxKey != null) 24f else 20f,
                )
            }
        }
        refreshAttackReady()
    }

    private fun startAttackAnim(
        kind: HeroAnimKind,
        duration: Float = 0.42f,
        specialSet: String? = null,
    ) {
        specialAnimSet = specialSet
        attackAnimDuration = duration
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
                specialAnimSet = null
                attackAnimDuration = 0.42f
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

    private fun spawnSpecialMeleeFx(vfx: SpecialVfxSpec) {
        val key = vfx.meleeFxKey ?: return
        val origin = slashFxOrigin()
        specialSkillFx += SpecialSkillFx(
            x = origin.first,
            y = origin.second,
            facing = facing,
            spriteKey = key,
            duration = vfx.meleeFxDuration,
            scale = vfx.meleeFxScale,
        )
        // 필살은 빔을 한 줄 더
        if (key == "adv_fx_finisher") {
            specialSkillFx += SpecialSkillFx(
                x = origin.first + facing.dirX() * 40f,
                y = origin.second + facing.dirY() * 28f,
                facing = facing,
                spriteKey = "adv_fx_beam",
                duration = 0.48f,
                scale = 1.2f,
            )
        }
        dungeonCombatFrame++
    }

    private fun lungeForward(map: DungeonFloor, distance: Float) {
        val nx = dungeonHeroX + facing.dirX() * distance
        val ny = dungeonHeroY + facing.dirY() * distance
        tryMoveDungeon(map, nx, ny)
    }

    private fun performMeleeSlash(
        map: DungeonFloor,
        damage: Int,
        emitCrescent: Boolean = true,
        slashPower: Float = 1f,
        slashDuration: Float = 0.34f,
    ) {
        val origin = slashFxOrigin()
        if (emitCrescent) {
            meleeSlashFx = MeleeSlashFx(
                x = origin.first,
                y = origin.second,
                facing = facing,
                duration = slashDuration,
                power = slashPower,
            )
        }
        val range = MELEE_RANGE * (0.92f + 0.18f * slashPower)
        val fx = facing.dirX()
        val fy = facing.dirY()
        val hits = map.monsters.filter { monster ->
            if (!monster.alive) return@filter false
            val dx = monster.x - dungeonHeroX
            val dy = monster.y - dungeonHeroY
            val dist = hypot(dx, dy)
            if (dist > range || dist < 1f) return@filter false
            val dot = (dx * fx + dy * fy) / dist
            dot >= MELEE_CONE_DOT
        }
        if (hits.isEmpty()) {
            say("칼날이 허공을 가른다.")
            return
        }
        hits.forEach { monster ->
            val kdx = monster.x - dungeonHeroX
            val kdy = monster.y - dungeonHeroY
            // 휘두르기 효과음은 이미 재생됨 — 여기서는 넉백만
            damageMonster(monster, damage, knockDx = kdx, knockDy = kdy, hitSfx = null)
        }
    }

    private fun slashFxOrigin(): Pair<Float, Float> =
        dungeonHeroX + facing.dirX() * 18f to dungeonHeroY + facing.dirY() * 10f - 28f

    private fun spawnProjectile(
        style: WeaponStyle,
        damage: Int,
        life: Float,
        fxSpriteKey: String? = null,
        impactSpriteKey: String? = null,
        radius: Float = 18f,
    ) {
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
            radius = radius,
            fxSpriteKey = fxSpriteKey,
            impactSpriteKey = impactSpriteKey,
        )
        say(if (style == WeaponStyle.BOW) "화살을 날렸다!" else "마력을 쏘아냈다!")
    }

    private fun damageMonster(
        monster: DungeonMonster,
        damage: Int,
        knockDx: Float = 0f,
        knockDy: Float = 0f,
        hitSfx: String? = "hit",
    ) {
        if (!monster.alive) return
        if (hitSfx != null) emitSfx(hitSfx)
        applyKnockback(monster, knockDx, knockDy)
        val mitigated = (damage - monster.armor).coerceAtLeast(1)
        monster.hp -= mitigated
        if (monster.hp > 0) {
            val armorNote = if (monster.armor > 0) " · 방어 -${monster.armor}" else ""
            say("${monster.name}에게 ${mitigated} 피해! (HP ${monster.hp}/${monster.maxHp}$armorNote)")
            refreshDungeonFloor()
            return
        }
        monster.alive = false
        monster.hp = 0
        refreshDungeonFloor()
        rewardMonsterKill(monster)
    }

    /** 맞은 방향으로 몬스터를 밀어낸다 (벽이면 가능한 만큼만). */
    private fun applyKnockback(monster: DungeonMonster, dx: Float, dy: Float) {
        val map = dungeonFloor ?: return
        val len = hypot(dx, dy)
        if (len < 0.01f) return
        val nx = dx / len
        val ny = dy / len
        for (step in 5 downTo 1) {
            val dist = KNOCKBACK_DISTANCE * step / 5f
            val tx = monster.x + nx * dist
            val ty = monster.y + ny * dist
            if (map.isWalkable(tx, ty)) {
                monster.x = tx
                monster.y = ty
                return
            }
        }
    }

    private fun rewardMonsterKill(monster: DungeonMonster) {
        val floor = dungeonFloorNumber
        val biome = currentBiome()
        val wild = biome != ExploreBiome.DUNGEON && biome != ExploreBiome.CASTLE
        val bossMult = if (monster.isBoss) 3 else 1
        val gold = ((if (wild) 14 else 18) + floor * (if (wild) 11 else 14) + Random.nextInt(0, 16)) * bossMult
        val exp = ((if (wild) 16 else 20) + floor * (if (wild) 10 else 12)) * bossMult
        if (monster.isBoss) {
            say("보스 ${monster.name}을(를) 쓰러뜨렸다! (+${gold}G, EXP +$exp)")
        } else {
            say("${monster.name}을(를) 쓰러뜨렸다! (+${gold}G, EXP +$exp)")
        }
        player = player.copy(gold = player.gold + gold)
        when (biome) {
            ExploreBiome.FOREST -> if (floor > player.forestDepth) player = player.copy(forestDepth = floor)
            ExploreBiome.DESERT -> if (floor > player.desertDepth) player = player.copy(desertDepth = floor)
            ExploreBiome.GLACIER -> if (floor > player.glacierDepth) player = player.copy(glacierDepth = floor)
            ExploreBiome.DUNGEON -> if (floor > player.dungeonDepth) player = player.copy(dungeonDepth = floor)
            ExploreBiome.CASTLE -> if (floor > player.castleDepth) player = player.copy(castleDepth = floor)
        }
        gainExp(exp)
        gainMercExp(exp)
        if (activeParty.isNotEmpty()) {
            say("원정대 용병도 경험을 쌓았다. (+${exp} EXP)")
        }
        val lootChance = if (monster.isBoss) 100 else 40
        if (Random.nextInt(100) < lootChance) {
            val loot = when (biome) {
                ExploreBiome.FOREST -> ItemCatalog.forestLoot.random()
                ExploreBiome.DESERT -> ItemCatalog.desertLoot.random()
                ExploreBiome.GLACIER -> ItemCatalog.glacierLoot.random()
                ExploreBiome.DUNGEON, ExploreBiome.CASTLE -> ItemCatalog.dungeonLoot.random()
            }
            addItem(loot)
            say(
                when {
                    monster.isBoss -> "보스의 잔해에서 ${loot.name}을(를) 챙겼다."
                    biome == ExploreBiome.FOREST -> "쓰러진 짐승 곁에서 ${loot.name}을(를) 챙겼다."
                    biome == ExploreBiome.DESERT -> "모래 속에서 ${loot.name}을(를) 주웠다."
                    biome == ExploreBiome.GLACIER -> "얼음 틈에서 ${loot.name}을(를) 챙겼다."
                    biome == ExploreBiome.CASTLE -> "해골 더미에서 ${loot.name}을(를) 챙겼다."
                    else -> "썩은 옷자락에서 ${loot.name}을(를) 챙겼다."
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
                    ExploreBiome.CASTLE ->
                        if (floor >= CastleFactory.MAX_FLOOR) {
                            "고성의 심층이 고요해진다. 저주의 핵이 흔들린다…"
                        } else {
                            "이 층의 언데드가 잠잠해졌다. 더 높은 성채로 올라가 보자."
                        }
                }
            )
            maybeLiberateCastle()
        }
    }

    /** Gray Castle 10층을 모두 정리하면 White Castle로 해방한다. */
    private fun maybeLiberateCastle() {
        if (currentBiome() != ExploreBiome.CASTLE) return
        if (dungeonFloorNumber < CastleFactory.MAX_FLOOR) return
        if (!mapCleared()) return
        if (player.castleCleared) return
        liberateCastle()
    }

    private fun liberateCastle() {
        player = player.copy(
            castleCleared = true,
            castleDepth = CastleFactory.MAX_FLOOR,
        )
        clearDungeonState()
        path.clear()
        pendingEnter = null
        walking = false
        currentSettlement = SettlementId.GRAY_CASTLE
        currentPlace = null
        scene = Scene.VILLAGE
        menuTab = MenuTab.NONE
        val home = placeOf(PlaceId.HOME)
        heroX = home.doorX
        heroY = home.doorY
        facing = Facing.DOWN
        resetPartyTrail(heroX, heroY)
        emitSfx("door")
        say("해골 왕의 왕관이 빛과 함께 산산이 부서진다!")
        say("저주가 풀렸다 — Gray Castle이 White Castle로 되살아난다.")
        say("해방된 사람들이 성으로 돌아와 다시 삶을 시작한다.")
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
            ExploreBiome.CASTLE -> CastleFactory.generate(floor)
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
        specialSkillFx.clear()
        specialAnimSet = null
        attackAnimDuration = 0.42f
        dungeonProjectiles.clear()
        attackCooldown = 0f
        resetPartyTrail(dungeonHeroX, dungeonHeroY)
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
                when {
                    DungeonFactory.isBossFloor(floor) -> {
                        val bossName = DungeonFactory.bossForFloor(floor).second
                        say("이 층의 주인이 깨어 있다 — 보스 『$bossName』. 하층 계단 근처를 경계하라.")
                    }
                    floor == 1 -> say("한때 포도주 보관소와 하수도였던 길이 좀비의 숨결로 가득하다.")
                    else -> say("더 깊은 곳에서 검붉은 기운이 피부를 찌른다. 좀비석이 가까워지는 기분이다.")
                }
            }
            ExploreBiome.CASTLE -> {
                if (floor > player.castleDepth) player = player.copy(castleDepth = floor)
                say("─── Gray Castle · ${floor}층 ───")
                when {
                    CastleFactory.isFinalFloor(floor) ->
                        say("왕좌의 방. 해골 왕이 저주의 핵을 움켜쥐고 있다. 쓰러뜨리면 성이 되살아난다.")
                    floor == 1 ->
                        say("회색 돌벽 사이로 해골병사와 유령기마병의 발소리가 울린다.")
                    else ->
                        say("성채가 더 깊어진다. 해골궁수의 시위 소리가 복도를 훑는다.")
                }
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
                    ExploreBiome.CASTLE -> "더 높은 성채로 이어지는 계단 위에 서야 한다."
                }
            )
            return
        }
        if (player.hp <= player.maxHp * 0.15f) {
            say("몸 상태로는 더 들어갈 수 없다. 물약을 쓰거나 마을로 돌아가자.")
            return
        }
        if (currentBiome() == ExploreBiome.CASTLE &&
            dungeonFloorNumber >= CastleFactory.MAX_FLOOR
        ) {
            say("이곳이 Gray Castle의 최심층이다. 언데드를 모두 처치해 저주를 끊어라.")
            return
        }
        emitSfx("door")
        enterExploreFloor(dungeonFloorNumber + 1)
    }

    fun escapeDungeon() {
        val map = dungeonFloor
        if (map != null && map.tileKindAt(dungeonHeroX, dungeonHeroY) != DungeonTile.STAIRS_UP) {
            say("상부로 가는 통로·계단에서만 탈출할 수 있다.")
            return
        }
        say(
            when (currentBiome()) {
                ExploreBiome.FOREST -> "마을 쪽 바람이 폐를 채운다. 숲속 짐승들은 여전히 깊은 곳에서 숨 쉰다."
                ExploreBiome.DESERT -> "마을 쪽 공기가 폐를 채운다. 모래 아래 괴물들은 여전히 숨 쉰다."
                ExploreBiome.GLACIER -> "마을 쪽 온기가 손을 녹인다. 극지의 짐승들은 여전히 얼음 너머에 있다."
                ExploreBiome.DUNGEON -> "지상의 공기가 폐를 채운다. 저주는 아직 지하에 웅크리고 있다."
                ExploreBiome.CASTLE -> "성문 밖 바람이 폐를 채운다. 고성의 저주는 아직 심층에 남았다."
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
            ExploreBiome.DUNGEON, ExploreBiome.CASTLE -> ItemCatalog.dungeonLoot
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
        val home = placeOf(PlaceId.HOME)
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
        tickMonsters(map, dt)
        tickAttackFx(dt)
        tickProjectiles(map, dt)

        if (attackCooldown > 0f) {
            attackCooldown = (attackCooldown - dt).coerceAtLeast(0f)
            if (attackCooldown <= 0f) refreshAttackReady()
        }
        if (specialCooldown > 0f) {
            specialCooldown = (specialCooldown - dt).coerceAtLeast(0f)
            if (specialCooldown <= 0f) {
                specialReady = true
                specialSkillRevision++
            }
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
                noteLeaderMove(dungeonHeroX, dungeonHeroY)
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
        noteLeaderMove(dungeonHeroX, dungeonHeroY)
        tickHeroAnim(dt, moving = true)
    }

    private fun tickAttackFx(dt: Float) {
        var dirty = false
        val slash = meleeSlashFx
        if (slash != null) {
            slash.age += dt
            if (!slash.alive) meleeSlashFx = null
            else {
                meleeSlashFx = slash.copy(age = slash.age)
            }
            dirty = true
        }
        if (specialSkillFx.isNotEmpty()) {
            val doomed = mutableListOf<SpecialSkillFx>()
            specialSkillFx.forEach { fx ->
                fx.age += dt
                if (!fx.alive) doomed += fx
            }
            if (doomed.isNotEmpty()) specialSkillFx.removeAll(doomed.toSet())
            dirty = true
        }
        if (dirty) dungeonCombatFrame++
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
                val hitSfx = when (p.style) {
                    WeaponStyle.BOW -> "arrow_hit"
                    WeaponStyle.MAGIC -> "magic_hit"
                    else -> "hit"
                }
                damageMonster(
                    monster = hit,
                    damage = p.damage,
                    knockDx = p.vx,
                    knockDy = p.vy,
                    hitSfx = hitSfx,
                )
                p.impactSpriteKey?.let { key ->
                    specialSkillFx += SpecialSkillFx(
                        x = hit.x,
                        y = hit.y - 24f,
                        facing = facing,
                        spriteKey = key,
                        duration = 0.42f,
                        scale = 1.35f,
                    )
                }
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

    private fun monsterChaseSpeed(monster: DungeonMonster): Float {
        val base = when (monster.kind) {
            "runner" -> 110f
            "farmer" -> 72f
            "shambler" -> 62f
            "blacksmith" -> 58f
            "armored" -> 52f
            "golem" -> 46f
            "bloater" -> 42f
            "boss_warden" -> 78f
            "boss_abomination" -> 62f
            "boss_lich" -> 74f
            "ghost_cavalry" -> 105f
            "skel_archer" -> 70f
            "skel_soldier" -> 64f
            "boss_skel_king" -> 82f
            "bear", "polar_bear", "giant_scorpion" -> 68f
            else -> 58f
        }
        return if (monster.isBoss) base * 1.28f + 14f else base
    }

    private fun monsterAggroRange(monster: DungeonMonster): Float =
        if (monster.isBoss) MONSTER_AGGRO_RANGE + 55f else MONSTER_AGGRO_RANGE

    private fun monsterAttackRange(monster: DungeonMonster): Float =
        if (monster.isBoss) MONSTER_ATTACK_RANGE + 30f else MONSTER_ATTACK_RANGE

    private fun monsterAttackCooldown(monster: DungeonMonster): Float =
        if (monster.isBoss) MONSTER_ATTACK_COOLDOWN + 0.35f else MONSTER_ATTACK_COOLDOWN

    /** 가방·장비·스킬맵 등 UI가 열려 있으면 몬스터 AI를 멈춘다. */
    private fun dungeonMenusPauseMonsters(): Boolean =
        menuTab != MenuTab.NONE || levelUpSkillOffer != null

    /**
     * 주인공이 가까이 오면 추격하고, 사거리에 들어오면 공격 애니와 함께 타격한다.
     * 멀리 있는 적은 가끔 배회한다.
     */
    private fun tickMonsters(map: DungeonFloor, dt: Float) {
        if (dungeonCombatLock || dungeonMenusPauseMonsters()) return
        var dirty = false
        monsterWanderAcc += dt
        val doWander = monsterWanderAcc >= 0.55f
        if (doWander) monsterWanderAcc = 0f

        map.monsters.filter { it.alive }.forEach { monster ->
            if (monster.attackCooldown > 0f) {
                monster.attackCooldown = (monster.attackCooldown - dt).coerceAtLeast(0f)
            }

            // 공격 애니 재생 중
            if (monster.attacking) {
                monster.animTime += dt
                monster.animFrame =
                    ((monster.animTime / MONSTER_ATTACK_DURATION) * 4f).toInt().coerceIn(0, 3)
                if (!monster.attackHitApplied && monster.animTime >= MONSTER_ATTACK_DURATION * 0.42f) {
                    monster.attackHitApplied = true
                    val dist = hypot(dungeonHeroX - monster.x, dungeonHeroY - monster.y)
                    if (dist <= monsterAttackRange(monster) + 14f) {
                        resolveMonsterAttackHit(monster)
                    }
                }
                if (monster.animTime >= MONSTER_ATTACK_DURATION) {
                    monster.attacking = false
                    monster.animTime = 0f
                    monster.animFrame = 0
                    monster.moving = false
                }
                dirty = true
                return@forEach
            }

            val dx = dungeonHeroX - monster.x
            val dy = dungeonHeroY - monster.y
            val dist = hypot(dx, dy)
            val aggro = monsterAggroRange(monster)
            val atkRange = monsterAttackRange(monster)

            if (dist <= aggro && dist > 0.5f) {
                monster.facingLeft = dx < 0f
                if (dist <= atkRange && monster.attackCooldown <= 0f) {
                    monster.attacking = true
                    monster.attackHitApplied = false
                    monster.animTime = 0f
                    monster.animFrame = 0
                    monster.moving = false
                    monster.attackCooldown = monsterAttackCooldown(monster)
                    dirty = true
                } else {
                    val speed = monsterChaseSpeed(monster)
                    val nx = monster.x + dx / dist * speed * dt
                    val ny = monster.y + dy / dist * speed * dt
                    if (tryMoveMonster(map, monster, nx, ny)) {
                        monster.moving = true
                        monster.animTime += dt
                        monster.animFrame = (((monster.animTime * 7f).toInt() % 4) + 4) % 4
                        dirty = true
                    } else {
                        // 축 분리 시도
                        val movedX = tryMoveMonster(map, monster, nx, monster.y)
                        val movedY = tryMoveMonster(map, monster, monster.x, ny)
                        monster.moving = movedX || movedY
                        if (monster.moving) {
                            monster.animTime += dt
                            monster.animFrame = (((monster.animTime * 7f).toInt() % 4) + 4) % 4
                            dirty = true
                        }
                    }
                }
            } else {
                // 어그로 밖 — 가끔 배회
                if (monster.moving) {
                    monster.moving = false
                    monster.animFrame = 0
                    dirty = true
                }
                if (doWander) {
                    val ang = Random.nextFloat() * (Math.PI * 2).toFloat()
                    val step = Random.nextFloat() * 22f
                    val nx = monster.x + cos(ang) * step
                    val ny = monster.y + sin(ang) * step
                    if (tryMoveMonster(map, monster, nx, ny)) {
                        monster.facingLeft = cos(ang) < 0f
                        dirty = true
                    }
                }
            }
        }

        if (dirty) {
            refreshDungeonFloor()
            dungeonCombatFrame++
        }
    }

    private fun tryMoveMonster(map: DungeonFloor, monster: DungeonMonster, x: Float, y: Float): Boolean {
        val r = if (monster.isBoss) 18f else 12f
        if (!map.isWalkable(x, y) ||
            !map.isWalkable(x - r, y) || !map.isWalkable(x + r, y) ||
            !map.isWalkable(x, y - r) || !map.isWalkable(x, y + r)
        ) return false
        monster.x = x
        monster.y = y
        return true
    }

    private fun resolveMonsterAttackHit(monster: DungeonMonster) {
        val base = (monster.power - totalDef).coerceAtLeast(3) + Random.nextInt(0, 4)
        val dmg = if (monster.isBoss) {
            (base * 1.55f).toInt().coerceAtLeast(base + 8)
        } else {
            base
        }
        val biome = currentBiome()
        say(
            when {
                monster.isBoss -> "보스 ${monster.name}의 일격! (HP -$dmg)"
                biome == ExploreBiome.FOREST -> "${monster.name}이(가) 덮친다! (HP -$dmg)"
                biome == ExploreBiome.DESERT -> "${monster.name}이(가) 찌른다! (HP -$dmg)"
                biome == ExploreBiome.GLACIER -> "${monster.name}이(가) 할퀸다! (HP -$dmg)"
                biome == ExploreBiome.CASTLE -> "${monster.name}이(가) 덮쳐온다! (HP -$dmg)"
                else -> "${monster.name}이(가) 물어뜯는다! (HP -$dmg)"
            }
        )
        emitSfx("hit")
        applyFrontDamage(dmg)
        val push = 34f
        val dx = dungeonHeroX - monster.x
        val dy = dungeonHeroY - monster.y
        val len = hypot(dx, dy).coerceAtLeast(0.01f)
        val map = dungeonFloor ?: return
        tryMoveDungeon(
            map,
            dungeonHeroX + dx / len * push,
            dungeonHeroY + dy / len * push
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
        val levelsGained = mutableListOf<Int>()
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
            levelsGained += p.level
            log.add("레벨 업! Lv.${p.level} 이(가) 되었다. 몸이 가벼워졌다.")
        }
        player = p
        if (levelsGained.isNotEmpty()) {
            onActorLevelUp(HERO_SKILL_KEY, p.name, ActorClass.ADVENTURER, levelsGained)
        }
    }

    // ---------------------------------------------------------------- 표시용

    fun equippedSummary(): List<Pair<ItemType, EquippedItem?>> =
        EQUIP_SLOTS.map { it to equipment[it] }
}
