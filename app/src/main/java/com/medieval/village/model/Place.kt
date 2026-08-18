package com.medieval.village.model

enum class PlaceId {
    HOME, SHOP, WEAPON_SHOP, HOSPITAL, CHURCH, INN,
    PUB, ARENA, DUNGEON, EAST_FOREST, SOUTH_DESERT, NORTH_GLACIER,
    BLACKSMITH, MAGIC_SCHOOL, MERCENARY
}

/** 건물 외형 종류 (Canvas 렌더링 분기용) */
enum class BuildingStyle {
    HOUSE, STORE, ARMORY, CLINIC, CHURCH, INN, PUB, ARENA, CAVE, FOREST, DESERT, GLACIER, FORGE, TOWER, CAMP
}

data class Place(
    val id: PlaceId,
    val name: String,
    val subtitle: String,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val style: BuildingStyle,
    val roof: Long,
    val wall: Long
) {
    val left: Float get() = cx - w / 2f
    val top: Float get() = cy - h / 2f
    val right: Float get() = cx + w / 2f
    val bottom: Float get() = cy + h / 2f

    /** 주인공이 서게 되는 문 앞 좌표 (항상 길 위) */
    val doorX: Float get() = cx
    val doorY: Float get() = bottom + Village.DOOR_GAP

    /** 맵에 합성으로 얹는 커스텀 건물 스프라이트 키 (오크헤이븐 전용) */
    val overlayKey: String? get() = when (style) {
        BuildingStyle.FORGE -> "forge"
        BuildingStyle.TOWER -> "tower"
        BuildingStyle.ARENA -> "arena"
        BuildingStyle.CAMP -> "camp"
        else -> null
    }
}

/**
 * 마을 맵 공통 좌표계(1536×1024).
 * 정착지별 핫스팟은 [Settlements]를 본다. 하위 호환용으로 오크헤이븐 데이터를 노출한다.
 */
object Village {

    /** 마을 맵 픽셀 크기와 1:1 */
    const val W = 1536f
    const val H = 1024f

    const val ROAD_X = 768f
    const val ROAD_W = 90f
    const val DOOR_GAP = 16f

    val rowRoads = listOf(320f, 520f, 700f, 880f)
    const val ROW_ROAD_LEFT = 160f
    const val ROW_ROAD_RIGHT = 1300f

    const val ROAD_TOP = 220f
    const val BOTTOM_ROAD_Y = 900f
    const val BOTTOM_ROAD_LEFT = 300f
    const val BOTTOM_ROAD_RIGHT = 1100f

    val places: List<Place> get() = Settlements.oakhaven.places

    fun of(id: PlaceId): Place = Settlements.oakhaven.of(id)

    val trees = emptyList<Triple<Float, Float, Float>>()
    val lamps = emptyList<Pair<Float, Float>>()
    val fences = emptyList<Pair<Float, Float>>()

    const val WELL_X = 700f
    const val WELL_Y = 500f

    val stalls = emptyList<Triple<Float, Float, Int>>()

    val townsfolk: List<Triple<String, Float, Float>> get() = Settlements.oakhaven.townsfolk
}
