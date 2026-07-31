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
            PlaceId.CHURCH, "교회", "저주를 씻는 신전",
            500f, 150f, 250f, 180f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
        ),
        // 좌측 열
        Place(
            PlaceId.MAGIC_SCHOOL, "마법학교", "해독 연금 학당",
            175f, 210f, SIDE_W, SIDE_H, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
        ),
        Place(
            PlaceId.BLACKSMITH, "대장간", "좀비 이빨을 부수는 모루",
            175f, 570f, SIDE_W, SIDE_H, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
        ),
        Place(
            PlaceId.WEAPON_SHOP, "무기점", "생사자 대비 병기",
            175f, 870f, SIDE_W, SIDE_H, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
        ),
        Place(
            PlaceId.SHOP, "상점", "횃불과 붕대의 잡화",
            175f, 1150f, SIDE_W, SIDE_H, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
        ),
        // 우측 열
        Place(
            PlaceId.DUNGEON, "던전입구", "좀비 둥지 · 오염된 지하",
            825f, 210f, SIDE_W, SIDE_H, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
        ),
        Place(
            PlaceId.ARENA, "대련소", "지상의 칼날 연마터",
            825f, 570f, SIDE_W, SIDE_H, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
        ),
        Place(
            PlaceId.MERCENARY, "용병고용소", "좀비 사냥 용병",
            825f, 870f, SIDE_W, SIDE_H, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
        ),
        Place(
            PlaceId.HOSPITAL, "병원", "오염 상처를 돌보는 집",
            825f, 1150f, SIDE_W, SIDE_H, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
        ),
        // 하단
        Place(
            PlaceId.INN, "INN", "여관 · 잠든 포도송이",
            290f, 1430f, 250f, 180f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
        ),
        Place(
            PlaceId.HOME, "주인공 집", "풍요의 마을 오두막",
            700f, 1430f, 250f, 180f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
        ),
        Place(
            PlaceId.PUB, "PUB", "신성한 잔 선술집",
            625f, 390f, 140f, 120f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
        )
    )

    fun of(id: PlaceId): Place = places.first { it.id == id }

    /** 나무/덤불 (x, y, 크기) — Sample처럼 건물 사이·길가를 채운다 */
    val trees = listOf(
        Triple(360f, 480f, 46f), Triple(370f, 900f, 42f),
        Triple(345f, 1180f, 38f), Triple(660f, 250f, 36f),
        Triple(105f, 1360f, 44f), Triple(915f, 1400f, 46f),
        Triple(150f, 1615f, 38f), Triple(870f, 1600f, 42f),
        Triple(90f, 300f, 40f), Triple(90f, 480f, 36f),
        Triple(90f, 720f, 42f), Triple(90f, 980f, 38f),
        Triple(910f, 300f, 40f), Triple(910f, 480f, 36f),
        Triple(910f, 720f, 42f), Triple(910f, 980f, 38f),
        Triple(300f, 160f, 34f), Triple(700f, 160f, 34f),
        Triple(430f, 640f, 32f), Triple(570f, 640f, 32f),
        Triple(430f, 980f, 32f), Triple(570f, 980f, 32f),
        Triple(500f, 1200f, 36f), Triple(500f, 1500f, 40f),
        Triple(250f, 1320f, 34f), Triple(780f, 1320f, 34f),
        Triple(120f, 1100f, 30f), Triple(880f, 1100f, 30f),
    )

    /** 가로등/표지판 (x, y) */
    val lamps = listOf(
        420f to 500f, 580f to 500f, 420f to 1120f, 580f to 1120f, 420f to 860f, 580f to 860f,
        360f to 345f, 640f to 345f, 360f to 1285f, 640f to 1285f,
    )

    /** 울타리 구간 (x, y) */
    val fences = listOf(
        300f to 400f, 340f to 400f, 380f to 400f,
        620f to 400f, 660f to 400f, 700f to 400f,
        300f to 760f, 340f to 760f,
        660f to 760f, 700f to 760f,
        240f to 1520f, 280f to 1520f, 720f to 1520f, 760f to 1520f,
    )

    /** 광장 우물 위치 */
    const val WELL_X = 640f
    const val WELL_Y = 830f

    /** 장터 좌판 (x, y, 천막 색 인덱스) */
    val stalls = listOf(
        Triple(352f, 620f, 0), Triple(628f, 1190f, 1),
        Triple(480f, 720f, 0), Triple(520f, 1100f, 1),
    )
}
