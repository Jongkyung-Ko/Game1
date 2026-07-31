package com.medieval.village.model

enum class PlaceId {
    HOME, SHOP, WEAPON_SHOP, HOSPITAL, CHURCH, INN,
    PUB, ARENA, DUNGEON, BLACKSMITH, MAGIC_SCHOOL, MERCENARY
}

/** 건물 외형 종류 (Canvas 렌더링 분기용) */
enum class BuildingStyle {
    HOUSE, STORE, ARMORY, CLINIC, CHURCH, INN, PUB, ARENA, CAVE, FORGE, TOWER, CAMP
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
}

/**
 * 커스텀 마을 일러스트(가로형)에 맞춘 좌표계.
 * 핫스팟은 그림 위 건물 위치에 대응한다.
 */
object Village {

    /** option-b 마을 맵 비율(1536×1024)에 맞춤 */
    const val W = 1200f
    const val H = 800f

    const val ROAD_X = 600f
    const val ROAD_W = 70f
    const val DOOR_GAP = 18f

    val rowRoads = listOf(280f, 420f, 560f, 680f)
    const val ROW_ROAD_LEFT = 180f
    const val ROW_ROAD_RIGHT = 1020f

    const val ROAD_TOP = 200f
    const val BOTTOM_ROAD_Y = 720f
    const val BOTTOM_ROAD_LEFT = 280f
    const val BOTTOM_ROAD_RIGHT = 900f

    private const val SPOT_W = 120f
    private const val SPOT_H = 90f

    val places: List<Place> = listOf(
        Place(
            PlaceId.CHURCH, "교회", "저주를 씻는 신전",
            600f, 150f, 140f, 100f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
        ),
        Place(
            PlaceId.DUNGEON, "던전입구", "좀비 둥지 · 오염된 지하",
            200f, 170f, SPOT_W, SPOT_H, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
        ),
        Place(
            PlaceId.MAGIC_SCHOOL, "마법학교", "해독 연금 학당",
            420f, 220f, SPOT_W, SPOT_H, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
        ),
        Place(
            PlaceId.PUB, "PUB", "신성한 잔 선술집",
            920f, 200f, 130f, 95f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
        ),
        Place(
            PlaceId.BLACKSMITH, "대장간", "좀비 이빨을 부수는 모루",
            220f, 380f, SPOT_W, SPOT_H, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
        ),
        Place(
            PlaceId.SHOP, "상점", "횃불과 붕대의 잡화",
            380f, 420f, SPOT_W, SPOT_H, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
        ),
        Place(
            PlaceId.WEAPON_SHOP, "무기점", "생사자 대비 병기",
            180f, 520f, SPOT_W, SPOT_H, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
        ),
        Place(
            PlaceId.ARENA, "대련소", "지상의 칼날 연마터",
            1000f, 420f, SPOT_W, SPOT_H, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
        ),
        Place(
            PlaceId.MERCENARY, "용병고용소", "좀비 사냥 용병",
            860f, 480f, SPOT_W, SPOT_H, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
        ),
        Place(
            PlaceId.HOSPITAL, "병원", "오염 상처를 돌보는 집",
            920f, 600f, SPOT_W, SPOT_H, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
        ),
        Place(
            PlaceId.INN, "INN", "여관 · 잠든 포도송이",
            680f, 560f, 130f, 95f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
        ),
        Place(
            PlaceId.HOME, "주인공 집", "풍요의 마을 오두막",
            280f, 620f, 130f, 95f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
        ),
    )

    fun of(id: PlaceId): Place = places.first { it.id == id }

    val trees = emptyList<Triple<Float, Float, Float>>()
    val lamps = emptyList<Pair<Float, Float>>()
    val fences = emptyList<Pair<Float, Float>>()

    const val WELL_X = 540f
    const val WELL_Y = 420f

    val stalls = emptyList<Triple<Float, Float, Int>>()
}
