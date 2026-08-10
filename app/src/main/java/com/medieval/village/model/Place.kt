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
}

/**
 * 오크헤이븐 마을 일러스트(1536×1024)에 맞춘 좌표계.
 * 핫스팟은 그림 위 건물 위치에 대응한다.
 */
object Village {

    /** oakhaven 마을 맵 픽셀 크기와 1:1 */
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

    val places: List<Place> = listOf(
        // 황금 잔 여관 (좌상단 대형 건물)
        Place(
            PlaceId.PUB, "선술집", "황금 잔 · Golden Tankard",
            300f, 240f, 200f, 150f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
        ),
        Place(
            PlaceId.INN, "여관", "잠자리와 따뜻한 식사",
            420f, 280f, 130f, 100f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
        ),
        // 푸른 지붕 마법탑
        Place(
            PlaceId.MAGIC_SCHOOL, "마법학교", "오크헤이븐 연금 탑",
            500f, 190f, 110f, 150f, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
        ),
        // 북쪽 빙하 입구
        Place(
            PlaceId.NORTH_GLACIER, "북쪽 빙하", "극지의 길 · Northern Glacier",
            780f, 90f, 150f, 110f, BuildingStyle.GLACIER, 0xFF6A90B0, 0xFFE0F0F8
        ),
        // 성당 + 공동묘지
        Place(
            PlaceId.CHURCH, "교회", "성 알라릭 예배당",
            1120f, 250f, 160f, 150f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
        ),
        // 우측 절벽 동굴
        Place(
            PlaceId.DUNGEON, "던전입구", "저주받은 동굴 · 지하묘소",
            1380f, 200f, 130f, 110f, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
        ),
        // 동부 숲 입구 (우측 중하단)
        Place(
            PlaceId.EAST_FOREST, "동쪽 숲", "야생의 길 · Eastern Forest",
            1420f, 560f, 150f, 130f, BuildingStyle.FOREST, 0xFF2F4A28, 0xFF6B8F4E
        ),
        // 광장 시장 포장
        Place(
            PlaceId.SHOP, "상점", "광장 잡화 노점",
            640f, 540f, 150f, 100f, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
        ),
        // 합성 대장간
        Place(
            PlaceId.BLACKSMITH, "대장간", "모루와 불꽃의 공방",
            210f, 520f, 170f, 140f, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
        ),
        Place(
            PlaceId.WEAPON_SHOP, "무기점", "생사자 대비 병기",
            340f, 560f, 120f, 90f, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
        ),
        // 약초원 오두막 (좌하단)
        Place(
            PlaceId.HOSPITAL, "병원", "약초와 치료의 집",
            260f, 700f, 150f, 110f, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
        ),
        // 주거 단지 (하단 중앙)
        Place(
            PlaceId.HOME, "주인공 집", "오크헤이븐 오두막",
            720f, 780f, 190f, 130f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
        ),
        // 합성 훈련장
        Place(
            PlaceId.ARENA, "대련소", "말뚝 울타리 훈련장",
            400f, 880f, 180f, 120f, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
        ),
        // 남쪽 사막 입구
        Place(
            PlaceId.SOUTH_DESERT, "남쪽 사막", "모래바람 · Southern Desert",
            900f, 930f, 160f, 110f, BuildingStyle.DESERT, 0xFFA07030, 0xFFE8C878
        ),
        // 합성 용병 야영지
        Place(
            PlaceId.MERCENARY, "용병고용소", "강변 용병 야영",
            1080f, 860f, 180f, 120f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
        ),
    )

    fun of(id: PlaceId): Place = places.first { it.id == id }

    val trees = emptyList<Triple<Float, Float, Float>>()
    val lamps = emptyList<Pair<Float, Float>>()
    val fences = emptyList<Pair<Float, Float>>()

    const val WELL_X = 768f
    const val WELL_Y = 520f

    val stalls = emptyList<Triple<Float, Float, Int>>()
}
