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

    /** 맵에 합성으로 얹는 커스텀 건물 스프라이트 키 */
    val overlayKey: String? get() = when (style) {
        BuildingStyle.FORGE -> "forge"
        BuildingStyle.TOWER -> "tower"
        BuildingStyle.ARENA -> "arena"
        BuildingStyle.CAMP -> "camp"
        else -> null
    }
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
        // 황금 잔 선술집 (좌상단 대형 목조 건물)
        Place(
            PlaceId.PUB, "선술집", "황금 잔 · Golden Tankard",
            340f, 345f, 300f, 250f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
        ),
        // 선술집 테라스·입구 쪽 여관
        Place(
            PlaceId.INN, "여관", "잠자리와 따뜻한 식사",
            450f, 420f, 130f, 105f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
        ),
        // 합성 마법탑 (파란 지붕)
        Place(
            PlaceId.MAGIC_SCHOOL, "마법학교", "오크헤이븐 연금 탑",
            600f, 200f, 130f, 160f, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
        ),
        // 북쪽 숲길
        Place(
            PlaceId.NORTH_GLACIER, "북쪽 빙하", "극지의 길 · Northern Glacier",
            770f, 100f, 135f, 85f, BuildingStyle.GLACIER, 0xFF6A90B0, 0xFFE0F0F8
        ),
        // 성당 + 종탑
        Place(
            PlaceId.CHURCH, "교회", "성 알라릭 예배당",
            1020f, 280f, 215f, 245f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
        ),
        // 우측 절벽 동굴
        Place(
            PlaceId.DUNGEON, "던전입구", "저주받은 동굴 · 지하묘소",
            1360f, 145f, 125f, 105f, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
        ),
        // 동부 숲 입구
        Place(
            PlaceId.EAST_FOREST, "동쪽 숲", "야생의 길 · Eastern Forest",
            1465f, 540f, 115f, 150f, BuildingStyle.FOREST, 0xFF2F4A28, 0xFF6B8F4E
        ),
        // 광장 잡화 노점 (우물 서쪽 포장)
        Place(
            PlaceId.SHOP, "상점", "광장 잡화 노점",
            575f, 560f, 150f, 105f, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
        ),
        // 합성 대장간
        Place(
            PlaceId.BLACKSMITH, "대장간", "모루와 불꽃의 공방",
            215f, 460f, 170f, 150f, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
        ),
        Place(
            PlaceId.WEAPON_SHOP, "무기점", "생사자 대비 병기",
            355f, 510f, 115f, 95f, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
        ),
        // 약초원 오두막
        Place(
            PlaceId.HOSPITAL, "병원", "약초와 치료의 집",
            255f, 610f, 180f, 150f, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
        ),
        // 하단 주거 단지
        Place(
            PlaceId.HOME, "주인공 집", "오크헤이븐 오두막",
            800f, 800f, 300f, 200f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
        ),
        // 합성 훈련장
        Place(
            PlaceId.ARENA, "대련소", "말뚝 울타리 훈련장",
            400f, 905f, 175f, 125f, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
        ),
        // 남쪽 길
        Place(
            PlaceId.SOUTH_DESERT, "남쪽 사막", "모래바람 · Southern Desert",
            880f, 955f, 155f, 80f, BuildingStyle.DESERT, 0xFFA07030, 0xFFE8C878
        ),
        // 합성 용병 야영지
        Place(
            PlaceId.MERCENARY, "용병고용소", "강변 용병 야영",
            1170f, 855f, 190f, 140f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
        ),
    )

    fun of(id: PlaceId): Place = places.first { it.id == id }

    val trees = emptyList<Triple<Float, Float, Float>>()
    val lamps = emptyList<Pair<Float, Float>>()
    val fences = emptyList<Pair<Float, Float>>()

    const val WELL_X = 700f
    const val WELL_Y = 500f

    val stalls = emptyList<Triple<Float, Float, Int>>()

    /** 마을 광장·길 위 주민 (스프라이트 키, x, y) */
    val townsfolk: List<Triple<String, Float, Float>> = listOf(
        Triple("farmer", 520f, 620f),
        Triple("merchant", 660f, 575f),
        Triple("shopkeeper", 740f, 560f),
        Triple("teacher", 920f, 420f),
        Triple("chef", 430f, 480f),
        Triple("doctor", 320f, 700f),
    )
}
