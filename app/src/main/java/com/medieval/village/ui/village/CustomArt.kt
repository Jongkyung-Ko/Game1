package com.medieval.village.ui.village

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.medieval.village.game.Facing
import com.medieval.village.model.SettlementId
import com.medieval.village.model.Settlements
import com.medieval.village.model.SpecialSkillCatalog
import com.medieval.village.model.Village
import kotlin.math.roundToInt

/** 커스텀 마을 맵 + 캐릭터·건물 스프라이트. 로딩 실패 시 null을 허용한다. */
class CustomArt(
    private val villageMaps: Map<String, ImageBitmap>,
    val continentMap: ImageBitmap?,
    private val chars: Map<String, ImageBitmap>,
    private val heroes: Map<String, ImageBitmap>,
    private val buildings: Map<String, ImageBitmap>,
    private val interiors: Map<String, ImageBitmap>,
    private val heroAnims: Map<String, List<ImageBitmap>>,
    private val skillIcons: Map<String, ImageBitmap> = emptyMap(),
) {
    /** 하위 호환: 기본(오크헤이븐) 맵 */
    val villageMap: ImageBitmap
        get() = villageMaps["oakhaven_base.png"]
            ?: villageMaps["village_map.png"]
            ?: villageMaps.values.first()

    fun villageMapFor(id: SettlementId, castleCleared: Boolean = false): ImageBitmap? {
        val asset = Settlements.of(id, castleCleared).mapAsset
        return villageMaps[asset] ?: villageMaps["oakhaven_base.png"]
    }

    fun villageMapForAsset(mapAsset: String): ImageBitmap? =
        villageMaps[mapAsset] ?: villageMapFor(SettlementId.OAKHAVEN)
    fun charOrNull(name: String): ImageBitmap? = chars[name]

    fun npcSpriteOrNull(key: String): ImageBitmap? = charOrNull(key)

    fun heroSpriteOrNull(facingKey: String): ImageBitmap? = heroes[facingKey]

    fun buildingOrNull(key: String): ImageBitmap? = buildings[key]

    fun interiorOrNull(key: String): ImageBitmap? = interiors[key]

    fun skillIconOrNull(skillId: String): ImageBitmap? = skillIcons[skillId]

    fun heroAnimFrameOrNull(set: String, frame: Int): ImageBitmap? {
        val list = heroAnims[set] ?: return null
        if (list.isEmpty()) return null
        return list[frame.coerceIn(0, list.lastIndex)]
    }

    fun hasHeroAnim(set: String): Boolean = heroAnims[set]?.isNotEmpty() == true

    fun zombieSpriteOrNull(kind: String): ImageBitmap? = when (kind) {
        "shambler" -> charOrNull("zombie_shambler")
        "runner" -> charOrNull("zombie_runner")
        "bloater" -> charOrNull("zombie_bloater")
        "armored" -> charOrNull("zombie_armored")
        "blacksmith" -> charOrNull("zombie_blacksmith")
        "farmer" -> charOrNull("zombie_farmer")
        "golem" -> charOrNull("golem_teacher")
        "boss_warden" -> charOrNull("zombie_boss_warden")
        "boss_abomination" -> charOrNull("zombie_boss_abomination")
        "boss_lich" -> charOrNull("zombie_boss_lich")
        "skel_soldier" -> charOrNull("skel_soldier")
        "ghost_cavalry" -> charOrNull("ghost_cavalry")
        "skel_archer" -> charOrNull("skel_archer")
        "boss_skel_king" -> charOrNull("boss_skel_king")
        else -> charOrNull("zombie_shambler")
    }

    /** 던전·야외 몬스터 애니메이션 세트 키 */
    fun monsterAnimKey(kind: String): String = when (kind) {
        // 던전 좀비
        "shambler", "runner", "bloater", "armored", "blacksmith", "farmer", "golem" -> kind
        "boss_warden", "boss_abomination", "boss_lich" -> kind
        // Gray Castle
        "skel_soldier", "ghost_cavalry", "skel_archer", "boss_skel_king" -> kind
        // 숲
        "wolf", "dire_wolf" -> "wolf"
        "bear" -> "bear"
        "boar", "giant_boar" -> "boar"
        "fox", "ice_fox", "snow_hare", "rabbit" -> "fox"
        "deer", "stag" -> "deer"
        "snake" -> "snake"
        "forest_spider", "camel_spider", "ice_spider" -> "spider"
        "owl", "frost_owl" -> "fox"
        // 사막
        "scorpion", "giant_scorpion", "deathstalker" -> "scorpion"
        "desert_fox" -> "desert_fox"
        "sand_snake", "sidewinder" -> "snake"
        "sand_golem" -> "sand_golem"
        "desert_drake" -> "desert_drake"
        "vulture" -> "desert_fox"
        "dung_beetle", "dune_worm" -> "scorpion"
        // 빙하
        "penguin", "ice_penguin", "frost_penguin", "seal" -> "penguin"
        "polar_bear" -> "polar_bear"
        "yeti" -> "yeti"
        "ice_wolf" -> "ice_wolf"
        "ice_elemental" -> "ice_elemental"
        else -> "wolf"
    }

    fun zombieAnimKey(kind: String): String = monsterAnimKey(kind)

    fun monsterAnimFrameOrNull(kind: String, attacking: Boolean, walking: Boolean, frame: Int): ImageBitmap? {
        val key = monsterAnimKey(kind)
        if (attacking) {
            heroAnimFrameOrNull("${key}_attack", frame)?.let { return it }
        }
        if (walking || attacking) {
            heroAnimFrameOrNull("${key}_walk", frame)?.let { return it }
        }
        heroAnimFrameOrNull("${key}_walk", 0)?.let { return it }
        return zombieSpriteOrNull(kind)
    }

    fun zombieAnimFrameOrNull(kind: String, attacking: Boolean, walking: Boolean, frame: Int): ImageBitmap? =
        monsterAnimFrameOrNull(kind, attacking, walking, frame)

    fun hasMonsterAnim(kind: String): Boolean =
        hasHeroAnim("${monsterAnimKey(kind)}_walk") || hasHeroAnim("${monsterAnimKey(kind)}_attack")

    fun hasZombieAnim(kind: String): Boolean = hasMonsterAnim(kind)

    companion object {
        private const val TAG = "CustomArt"

        @Volatile
        private var cached: CustomArt? = null

        private val CHAR_NAMES = listOf(
            "warrior", "rogue", "mage", "paladin",
            "merchant", "shopkeeper", "blacksmith", "doctor",
            "farmer", "teacher", "chef",
            "zombie_shambler", "zombie_runner", "zombie_bloater", "zombie_armored",
            "zombie_blacksmith", "zombie_farmer", "golem_teacher",
            "zombie_boss_warden", "zombie_boss_abomination", "zombie_boss_lich",
            // Gray Castle 언데드
            "skel_soldier", "ghost_cavalry", "skel_archer", "boss_skel_king",
        )

        private val HERO_KEYS = listOf("front", "back", "side", "portrait")
        private val BUILDING_KEYS = listOf("forge", "tower", "arena", "camp")
        private val INTERIOR_KEYS = listOf(
            "home", "shop", "weapon_shop",
            "inn", "hospital", "church",
            "blacksmith", "magic_school", "arena",
            "mercenary", "pub",
        )
        private val HERO_ANIM_SETS = listOf(
            "walk_side", "walk_down", "slash", "bow", "magic",
            // 주인공 특별스킬
            "adv_smash", "adv_flurry", "adv_charge", "adv_shot", "adv_bolt", "adv_finisher",
            "adv_fx_smash", "adv_fx_flurry", "adv_fx_charge", "adv_fx_finisher",
            "adv_fx_arrow", "adv_fx_firebolt", "adv_fx_fireburst", "adv_fx_beam",
            // 용병 애니메이션
            "warrior_walk", "warrior_slash",
            "rogue_walk", "rogue_slash",
            "paladin_walk", "paladin_slash",
            "mage_walk", "mage_cast",
            // 던전 몬스터 걷기/공격
            "shambler_walk", "shambler_attack",
            "runner_walk", "runner_attack",
            "bloater_walk", "bloater_attack",
            "armored_walk", "armored_attack",
            "blacksmith_walk", "blacksmith_attack",
            "farmer_walk", "farmer_attack",
            "golem_walk", "golem_attack",
            // 던전 보스
            "boss_warden_walk", "boss_warden_attack",
            "boss_abomination_walk", "boss_abomination_attack",
            "boss_lich_walk", "boss_lich_attack",
            // Gray Castle
            "skel_soldier_walk", "skel_soldier_attack",
            "ghost_cavalry_walk", "ghost_cavalry_attack",
            "skel_archer_walk", "skel_archer_attack",
            "boss_skel_king_walk", "boss_skel_king_attack",
            // 야외 몬스터 걷기/공격
            "wolf_walk", "wolf_attack",
            "bear_walk", "bear_attack",
            "boar_walk", "boar_attack",
            "fox_walk", "fox_attack",
            "deer_walk", "deer_attack",
            "spider_walk", "spider_attack",
            "scorpion_walk", "scorpion_attack",
            "snake_walk", "snake_attack",
            "sand_golem_walk", "sand_golem_attack",
            "desert_drake_walk", "desert_drake_attack",
            "desert_fox_walk", "desert_fox_attack",
            "polar_bear_walk", "polar_bear_attack",
            "yeti_walk", "yeti_attack",
            "penguin_walk", "penguin_attack",
            "ice_wolf_walk", "ice_wolf_attack",
            "ice_elemental_walk", "ice_elemental_attack",
        )
        private const val HERO_ANIM_FRAMES = 4

        fun loadOrNull(context: Context): CustomArt? {
            cached?.let { return it }
            return try {
                synchronized(this) {
                    cached?.let { return it }
                    val app = context.applicationContext
                    val villageMaps = LinkedHashMap<String, ImageBitmap>()
                    val mapAssets = buildSet {
                        Settlements.all(false).forEach { add(it.mapAsset) }
                        add(Settlements.castle(true).mapAsset)
                        add("village_map.png")
                    }
                    mapAssets.forEach { asset ->
                        loadAsset(app, "custom/$asset", cleanEdges = false)?.let {
                            villageMaps[asset] = it
                        }
                    }
                    if ("oakhaven_base.png" !in villageMaps && "village_map.png" in villageMaps) {
                        villageMaps["oakhaven_base.png"] = villageMaps.getValue("village_map.png")
                    }
                    if (villageMaps.isEmpty()) return null
                    val continent = loadAsset(app, "custom/continent_map.png", cleanEdges = false)
                    val chars = LinkedHashMap<String, ImageBitmap>()
                    CHAR_NAMES.forEach { name ->
                        loadAsset(app, "custom/chars/$name.png", cleanEdges = true)?.let {
                            chars[name] = it
                        }
                    }
                    val heroes = LinkedHashMap<String, ImageBitmap>()
                    HERO_KEYS.forEach { key ->
                        loadAsset(app, "custom/hero_$key.png", cleanEdges = true)?.let {
                            heroes[key] = it
                        }
                    }
                    val buildings = LinkedHashMap<String, ImageBitmap>()
                    BUILDING_KEYS.forEach { key ->
                        // 건물 일러스트는 이미 알파가 정리되어 있으므로 가장자리 가공 없이 로드
                        loadAsset(app, "custom/buildings/$key.png", cleanEdges = false)?.let {
                            buildings[key] = it
                        }
                    }
                    val interiors = LinkedHashMap<String, ImageBitmap>()
                    INTERIOR_KEYS.forEach { key ->
                        loadAsset(app, "custom/interiors/$key.png", cleanEdges = false)?.let {
                            interiors[key] = it
                        }
                    }
                    Log.i(TAG, "Loaded interiors: ${interiors.keys}")
                    val heroAnims = LinkedHashMap<String, List<ImageBitmap>>()
                    HERO_ANIM_SETS.forEach { set ->
                        val frames = ArrayList<ImageBitmap>(HERO_ANIM_FRAMES)
                        for (i in 0 until HERO_ANIM_FRAMES) {
                            loadAsset(
                                app,
                                "custom/hero_anim/frames/${set}_$i.png",
                                cleanEdges = true,
                            )?.let { frames += it }
                        }
                        if (frames.isNotEmpty()) heroAnims[set] = frames
                    }
                    Log.i(TAG, "Loaded hero anim sets: ${heroAnims.keys}")
                    val skillIcons = LinkedHashMap<String, ImageBitmap>()
                    SpecialSkillCatalog.all.forEach { skill ->
                        loadAsset(
                            app,
                            "custom/skills/${skill.id}.png",
                            cleanEdges = true,
                        )?.let { skillIcons[skill.id] = it }
                    }
                    Log.i(TAG, "Loaded skill icons: ${skillIcons.size}")
                    Log.i(TAG, "Loaded village maps: ${villageMaps.keys}")
                    val art = CustomArt(
                        villageMaps = villageMaps,
                        continentMap = continent,
                        chars = chars,
                        heroes = heroes,
                        buildings = buildings,
                        interiors = interiors,
                        heroAnims = heroAnims,
                        skillIcons = skillIcons,
                    )
                    cached = art
                    art
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load custom art", t)
                null
            }
        }

        private fun loadAsset(context: Context, path: String, cleanEdges: Boolean): ImageBitmap? {
            return try {
                val bytes = context.assets.open(path).use { it.readBytes() }
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inMutable = cleanEdges
                    inPremultiplied = true
                }
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    ?: return null
                val bmp = if (cleanEdges) {
                    val mutable = if (decoded.isMutable) {
                        decoded
                    } else {
                        decoded.copy(Bitmap.Config.ARGB_8888, true).also { decoded.recycle() }
                    }
                    hardenSpriteAlpha(mutable)
                    mutable
                } else {
                    decoded
                }
                bmp.asImageBitmap()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to decode $path", t)
                null
            }
        }

        private fun hardenSpriteAlpha(bmp: Bitmap) {
            if (!bmp.isMutable) return
            val w = bmp.width
            val h = bmp.height
            val px = IntArray(w * h)
            bmp.getPixels(px, 0, w, 0, 0, w, h)
            for (i in px.indices) {
                val c = px[i]
                val a = c ushr 24
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val nearWhite = r >= 245 && g >= 245 && b >= 245
                px[i] = when {
                    // 완전 투명 / 순백 배경만 제거. 중간 알파는 유지해 가장자리를 부드럽게.
                    a < 10 || nearWhite -> c and 0x00FFFFFF
                    a < 200 -> c // keep soft edge alpha
                    else -> c or 0xFF000000.toInt()
                }
            }
            bmp.setPixels(px, 0, w, 0, 0, w, h)
        }
    }
}

@Composable
fun rememberCustomArtOrNull(): CustomArt? {
    val context = LocalContext.current
    return remember(context) { CustomArt.loadOrNull(context) }
}

/** 하위 호환: 실패하면 예외 대신 null 경로를 쓰도록 화면 쪽에서 처리한다. */
@Composable
fun rememberCustomArt(): CustomArt {
    return rememberCustomArtOrNull()
        ?: error("Custom art assets missing — rebuild APK with assets/custom")
}

fun DrawScope.drawCustomVillageMap(
    art: CustomArt,
    settlementId: SettlementId = SettlementId.OAKHAVEN,
    mapAsset: String? = null,
    drawOverlays: Boolean = settlementId == SettlementId.OAKHAVEN,
) {
    val map = mapAsset?.let { art.villageMapForAsset(it) }
        ?: art.villageMapFor(settlementId)
        ?: art.villageMap
    drawImage(
        image = map,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(map.width, map.height),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(Village.W.roundToInt(), Village.H.roundToInt()),
        filterQuality = FilterQuality.Medium,
    )
    if (drawOverlays) {
        drawVillageBuildingOverlays(art, Settlements.of(settlementId).places)
    }
}

/** 맵에 없는 합성 건물(대장간·마법탑·대련소·용병캠프)을 핫스팟에 맞춰 그린다. */
fun DrawScope.drawVillageBuildingOverlays(
    art: CustomArt,
    places: List<com.medieval.village.model.Place> = Settlements.oakhaven.places,
) {
    places.forEach { place ->
        val key = place.overlayKey ?: return@forEach
        val image = art.buildingOrNull(key) ?: return@forEach
        val aspect = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
        val h = place.h * 1.05f
        val w = (h * aspect).coerceAtMost(place.w * 1.25f)
        val left = place.cx - w / 2f
        val top = place.bottom - h
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(w.roundToInt().coerceAtLeast(1), h.roundToInt().coerceAtLeast(1)),
            filterQuality = FilterQuality.Medium,
        )
    }
}

fun DrawScope.drawCustomSprite(
    image: ImageBitmap,
    cx: Float,
    footY: Float,
    worldHeight: Float,
    mirrorX: Boolean = false,
) {
    val aspect = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
    val h = worldHeight
    val w = h * aspect
    val left = cx - w / 2f
    val top = footY - h
    val dw = w.roundToInt().coerceAtLeast(1)
    val dh = h.roundToInt().coerceAtLeast(1)
    // 발 아래 부드러운 그림자
    drawOval(
        color = androidx.compose.ui.graphics.Color(0x33000000),
        topLeft = Offset(cx - w * 0.28f, footY - h * 0.04f),
        size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.08f),
    )
    if (mirrorX) {
        translate(left + w, top) {
            scale(-1f, 1f, pivot = Offset.Zero) {
                drawImage(
                    image = image,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(image.width, image.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(dw, dh),
                    alpha = 1f,
                    filterQuality = FilterQuality.Medium,
                )
            }
        }
    } else {
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(dw, dh),
            alpha = 1f,
            filterQuality = FilterQuality.Medium,
        )
    }
}

fun DrawScope.drawCustomHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    worldHeight: Float = 96f,
    walking: Boolean = false,
    walkPhase: Float = 0f,
    animKind: com.medieval.village.game.HeroAnimKind = com.medieval.village.game.HeroAnimKind.IDLE,
    animFrame: Int = 0,
    specialSet: String? = null,
) {
    drawAnimatedHero(
        art = art,
        x = x,
        y = y,
        facing = facing,
        walking = walking,
        walkPhase = walkPhase,
        worldHeight = worldHeight,
        animKind = animKind,
        animFrame = animFrame,
        specialSet = specialSet,
    )
}
