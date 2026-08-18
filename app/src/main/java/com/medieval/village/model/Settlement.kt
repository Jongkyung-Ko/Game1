package com.medieval.village.model

/** 대륙 위 정착지(마을·거점) */
enum class SettlementId {
    OAKHAVEN,
    ASHBROOK,
}

data class Settlement(
    val id: SettlementId,
    val nameKo: String,
    val nameEn: String,
    /** assets/custom 아래 마을 맵 파일명 */
    val mapAsset: String,
    /** 세계지도(1536×1024) 위 핀 좌표 */
    val mapX: Float,
    val mapY: Float,
    val blurb: String,
    val places: List<Place>,
    val townsfolk: List<Triple<String, Float, Float>> = emptyList(),
    val wellX: Float = 700f,
    val wellY: Float = 500f,
) {
    fun of(id: PlaceId): Place = places.first { it.id == id }

    fun ofOrNull(id: PlaceId): Place? = places.firstOrNull { it.id == id }

    val spawn: Place
        get() = ofOrNull(PlaceId.HOME) ?: places.first()
}

/**
 * 오크헤이븐 인근 대륙(에메랄드 해안)의 정착지 목록.
 * 맵·핫스팟은 각 마을 일러스트(1536×1024)에 맞춘다.
 */
object Settlements {

    val oakhaven: Settlement = Settlement(
        id = SettlementId.OAKHAVEN,
        nameKo = "오크헤이븐",
        nameEn = "Oakhaven",
        mapAsset = "oakhaven_base.png",
        mapX = 470f,
        mapY = 540f,
        blurb = "풍요의 마을 · 저주에 잠식된 고향",
        places = listOf(
            Place(
                PlaceId.PUB, "선술집", "황금 잔 · Golden Tankard",
                340f, 345f, 300f, 250f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
            ),
            Place(
                PlaceId.INN, "여관", "잠자리와 따뜻한 식사",
                450f, 420f, 130f, 105f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
            ),
            Place(
                PlaceId.MAGIC_SCHOOL, "마법학교", "오크헤이븐 연금 탑",
                600f, 200f, 130f, 160f, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
            ),
            Place(
                PlaceId.NORTH_GLACIER, "북쪽 빙하", "극지의 길 · Northern Glacier",
                770f, 100f, 135f, 85f, BuildingStyle.GLACIER, 0xFF6A90B0, 0xFFE0F0F8
            ),
            Place(
                PlaceId.CHURCH, "교회", "성 알라릭 예배당",
                1020f, 280f, 215f, 245f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
            ),
            Place(
                PlaceId.DUNGEON, "던전입구", "저주받은 동굴 · 지하묘소",
                1360f, 145f, 125f, 105f, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
            ),
            Place(
                PlaceId.EAST_FOREST, "동쪽 숲", "야생의 길 · Eastern Forest",
                1465f, 540f, 115f, 150f, BuildingStyle.FOREST, 0xFF2F4A28, 0xFF6B8F4E
            ),
            Place(
                PlaceId.SHOP, "상점", "광장 잡화 노점",
                575f, 560f, 150f, 105f, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
            ),
            Place(
                PlaceId.BLACKSMITH, "대장간", "모루와 불꽃의 공방",
                215f, 460f, 170f, 150f, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
            ),
            Place(
                PlaceId.WEAPON_SHOP, "무기점", "생사자 대비 병기",
                355f, 510f, 115f, 95f, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
            ),
            Place(
                PlaceId.HOSPITAL, "병원", "약초와 치료의 집",
                255f, 610f, 180f, 150f, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
            ),
            Place(
                PlaceId.HOME, "주인공 집", "오크헤이븐 오두막",
                800f, 800f, 300f, 200f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
            ),
            Place(
                PlaceId.ARENA, "대련소", "말뚝 울타리 훈련장",
                400f, 905f, 175f, 125f, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
            ),
            Place(
                PlaceId.SOUTH_DESERT, "남쪽 사막", "모래바람 · Southern Desert",
                880f, 955f, 155f, 80f, BuildingStyle.DESERT, 0xFFA07030, 0xFFE8C878
            ),
            Place(
                PlaceId.MERCENARY, "용병고용소", "강변 용병 야영",
                1170f, 855f, 190f, 140f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
            ),
        ),
        townsfolk = listOf(
            Triple("farmer", 520f, 620f),
            Triple("merchant", 660f, 575f),
            Triple("shopkeeper", 740f, 560f),
            Triple("teacher", 920f, 420f),
            Triple("chef", 430f, 480f),
            Triple("doctor", 320f, 700f),
        ),
        wellX = 700f,
        wellY = 500f,
    )

    /** 오크헤이븐 남동쪽 강변 마을 — 애쉬브룩 */
    val ashbrook: Settlement = Settlement(
        id = SettlementId.ASHBROOK,
        nameKo = "애쉬브룩",
        nameEn = "Ashbrook",
        mapAsset = "ashbrook_base.png",
        mapX = 640f,
        mapY = 620f,
        blurb = "강변의 재 마을 · 오크헤이븐 인근",
        places = listOf(
            // 강변 선술집·데크
            Place(
                PlaceId.PUB, "선술집", "강변의 재 · Ash Tankard",
                260f, 430f, 260f, 210f, BuildingStyle.PUB, 0xFF6B3A28, 0xFFC9A06A
            ),
            Place(
                PlaceId.INN, "여관", "부두 옆 여인숙",
                380f, 500f, 120f, 95f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
            ),
            // 파란 지붕 농가
            Place(
                PlaceId.HOME, "주인공 집", "애쉬브룩 농가",
                520f, 300f, 200f, 150f, BuildingStyle.HOUSE, 0xFF3A6B8C, 0xFFE8D4AC
            ),
            // 성당
            Place(
                PlaceId.CHURCH, "교회", "푸른 지붕 예배당",
                760f, 250f, 190f, 200f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
            ),
            // 광장 노점
            Place(
                PlaceId.SHOP, "상점", "분수 광장 노점",
                680f, 530f, 150f, 110f, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
            ),
            Place(
                PlaceId.WEAPON_SHOP, "무기점", "광장 병기 가판",
                780f, 560f, 110f, 90f, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
            ),
            // 대장간 (맵에 그려져 있음 — 오버레이 없음)
            Place(
                PlaceId.BLACKSMITH, "대장간", "모루 공방",
                1020f, 480f, 180f, 150f, BuildingStyle.CLINIC, 0xFF5A4132, 0xFF9B8266
            ),
            Place(
                PlaceId.HOSPITAL, "병원", "약초 오두막",
                430f, 620f, 150f, 120f, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
            ),
            // 절벽 동굴
            Place(
                PlaceId.DUNGEON, "던전입구", "절벽 동굴",
                1360f, 400f, 140f, 130f, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
            ),
            Place(
                PlaceId.EAST_FOREST, "동쪽 숲", "절벽 너머 숲길",
                1450f, 620f, 100f, 130f, BuildingStyle.FOREST, 0xFF2F4A28, 0xFF6B8F4E
            ),
            // 하단 주택가 → 대련·용병
            Place(
                PlaceId.ARENA, "대련소", "마을 훈련장",
                980f, 820f, 160f, 120f, BuildingStyle.HOUSE, 0xFF7A5230, 0xFFC9A87C
            ),
            Place(
                PlaceId.MERCENARY, "용병고용소", "다리 앞 야영",
                420f, 860f, 170f, 120f, BuildingStyle.HOUSE, 0xFF4E5A3A, 0xFF8B9668
            ),
            Place(
                PlaceId.MAGIC_SCHOOL, "마법학교", "작은 연구실",
                900f, 300f, 120f, 130f, BuildingStyle.HOUSE, 0xFF4B3B8F, 0xFFCFC7E8
            ),
            Place(
                PlaceId.SOUTH_DESERT, "남쪽 길", "평원으로 이어지는 길",
                700f, 950f, 150f, 70f, BuildingStyle.DESERT, 0xFFA07030, 0xFFE8C878
            ),
            Place(
                PlaceId.NORTH_GLACIER, "북쪽 언덕", "산록으로 가는 길",
                620f, 90f, 140f, 70f, BuildingStyle.GLACIER, 0xFF6A90B0, 0xFFE0F0F8
            ),
        ),
        townsfolk = listOf(
            Triple("farmer", 560f, 380f),
            Triple("merchant", 700f, 560f),
            Triple("shopkeeper", 640f, 540f),
            Triple("chef", 300f, 480f),
            Triple("blacksmith", 1000f, 540f),
            Triple("doctor", 450f, 660f),
        ),
        wellX = 640f,
        wellY = 500f,
    )

    val all: List<Settlement> = listOf(oakhaven, ashbrook)

    fun of(id: SettlementId): Settlement = when (id) {
        SettlementId.OAKHAVEN -> oakhaven
        SettlementId.ASHBROOK -> ashbrook
    }
}
