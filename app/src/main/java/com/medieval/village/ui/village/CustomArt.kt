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
import com.medieval.village.model.Village
import kotlin.math.roundToInt

/** 커스텀 마을 맵 + 캐릭터·건물 스프라이트. 로딩 실패 시 null을 허용한다. */
class CustomArt(
    val villageMap: ImageBitmap,
    private val chars: Map<String, ImageBitmap>,
    private val heroes: Map<String, ImageBitmap>,
    private val buildings: Map<String, ImageBitmap>,
    private val heroAnims: Map<String, List<ImageBitmap>>,
) {
    fun charOrNull(name: String): ImageBitmap? = chars[name]

    fun npcSpriteOrNull(key: String): ImageBitmap? = charOrNull(key)

    fun heroSpriteOrNull(facingKey: String): ImageBitmap? = heroes[facingKey]

    fun buildingOrNull(key: String): ImageBitmap? = buildings[key]

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
        else -> charOrNull("zombie_shambler")
    }

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
        )

        private val HERO_KEYS = listOf("front", "back", "side", "portrait")
        private val BUILDING_KEYS = listOf("forge", "tower", "arena", "camp")
        private val HERO_ANIM_SETS = listOf("walk_side", "walk_down", "slash", "bow", "magic")
        private const val HERO_ANIM_FRAMES = 4

        fun loadOrNull(context: Context): CustomArt? {
            cached?.let { return it }
            return try {
                synchronized(this) {
                    cached?.let { return it }
                    val app = context.applicationContext
                    val village = loadVillageMap(app) ?: return null
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
                    val art = CustomArt(
                        villageMap = village,
                        chars = chars,
                        heroes = heroes,
                        buildings = buildings,
                        heroAnims = heroAnims,
                    )
                    cached = art
                    art
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load custom art", t)
                null
            }
        }

        private fun loadVillageMap(context: Context): ImageBitmap? {
            // oakhaven_base 우선, 실패 시 village_map
            return loadAsset(context, "custom/oakhaven_base.png", cleanEdges = false)
                ?: loadAsset(context, "custom/village_map.png", cleanEdges = false)
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

fun DrawScope.drawCustomVillageMap(art: CustomArt) {
    drawImage(
        image = art.villageMap,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(art.villageMap.width, art.villageMap.height),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(Village.W.roundToInt(), Village.H.roundToInt()),
        filterQuality = FilterQuality.Medium,
    )
    drawVillageBuildingOverlays(art)
}

/** 맵에 없는 합성 건물(대장간·마법탑·대련소·용병캠프)을 핫스팟에 맞춰 그린다. */
fun DrawScope.drawVillageBuildingOverlays(art: CustomArt) {
    Village.places.forEach { place ->
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
    )
}
