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
 * 마을 좌표계. 폰 화면 하나에 전부 담기도록 고정 크기 월드를 정의하고,
 * 렌더링 시 화면 크기에 맞춰 균등 스케일한다.
 */
object Village {

    const val W = 1000f
    const val H = 1650f

    const val ROAD_X = 500f
    const val ROAD_W = 96f
    const val DOOR_GAP = 50f

    /** 좌우 건물 열이 붙는 가로길 y 좌표 */
    val rowRoads = listOf(345f, 705f, 1005f, 1285f)
    const val ROW_ROAD_LEFT = 175f
    const val ROW_ROAD_RIGHT = 825f

    const val ROAD_TOP = 250f
    const val BOTTOM_ROAD_Y = 1570f
    const val BOTTOM_ROAD_LEFT = 290f
    const val BOTTOM_ROAD_RIGHT = 700f

    private const val SIDE_W = 232f
    private const val SIDE_H = 170f

    val places: List<Place> = listOf(
        Place(
            PlaceId.CHURCH, "교회", "빛의 신전",
            500f, 150f, 250f, 180f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
        ),
        // 좌측 열
        Place(
            PlaceId.MAGIC_SCHOOL, "마법학교", "아르카나 학당",
            175f, 210f, SIDE_W, SIDE_H, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
        ),
        Place(
            PlaceId.BLACKSMITH, "대장간", "불꽃의 모루",
            175f, 570f, SIDE_W, SIDE_H, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
        ),
        Place(
            PlaceId.WEAPON_SHOP, "무기점", "강철과 가죽",
            175f, 870f, SIDE_W, SIDE_H, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
        ),
        Place(
            PlaceId.SHOP, "상점", "마을 잡화점",
            175f, 1150f, SIDE_W, SIDE_H, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
        ),
        // 우측 열
        Place(
            PlaceId.DUNGEON, "던전입구", "잊혀진 지하",
            825f, 210f, SIDE_W, SIDE_H, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
        ),
        Place(
            PlaceId.ARENA, "대련소", "무인들의 터",
            825f, 570f, SIDE_W, SIDE_H, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
        ),
        Place(
            PlaceId.MERCENARY, "용병고용소", "떠돌이 칼잡이",
            825f, 870f, SIDE_W, SIDE_H, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
        ),
        Place(
            PlaceId.HOSPITAL, "병원", "치유의 집",
            825f, 1150f, SIDE_W, SIDE_H, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
        ),
        // 하단
        Place(
            PlaceId.INN, "INN", "여관 · 잠든 곰",
            290f, 1430f, 250f, 180f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
        ),
        Place(
            PlaceId.HOME, "주인공 집", "나의 오두막",
            700f, 1430f, 250f, 180f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
        ),
        Place(
            PlaceId.PUB, "PUB", "황금 수사슴 선술집",
            625f, 390f, 140f, 120f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
        )
    )

    fun of(id: PlaceId): Place = places.first { it.id == id }

    /** 나무 (x, y, 크기) */
    val trees = listOf(
        Triple(360f, 480f, 46f), Triple(370f, 900f, 42f),
        Triple(345f, 1180f, 38f), Triple(660f, 250f, 36f),
        Triple(105f, 1360f, 44f), Triple(915f, 1400f, 46f),
        Triple(150f, 1615f, 38f), Triple(870f, 1600f, 42f)
    )

    /** 가로등 (x, y) */
    val lamps = listOf(
        420f to 500f, 580f to 500f, 420f to 1120f, 580f to 1120f, 420f to 860f, 580f to 860f
    )

    /** 광장 우물 위치 */
    const val WELL_X = 640f
    const val WELL_Y = 830f

    /** 장터 좌판 (x, y, 천막 색 인덱스) */
    val stalls = listOf(Triple(352f, 620f, 0), Triple(628f, 1190f, 1))
}
