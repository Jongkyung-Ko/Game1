package com.medieval.village.model

/** 대륙 위 정착지(마을·거점) */
enum class SettlementId {
    OAKHAVEN,
    ASHBROOK,
    GRAY_CASTLE,
    IGLOO,
    SEASIDE,
    WINTER_CASTLE,
}

/** 스토리 해방 여부 — 정착지 맵·대사가 갈린다 */
data class WorldFlags(
    val castleCleared: Boolean = false,
    val iglooCleared: Boolean = false,
    val seasideCleared: Boolean = false,
    val winterCleared: Boolean = false,
)

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

    /** 저주에 잠긴 고성 — 세계지도 핀 (북동 산록) */
    private val grayCastle: Settlement = Settlement(
        id = SettlementId.GRAY_CASTLE,
        nameKo = "회색 성",
        nameEn = "Gray Castle",
        mapAsset = "gray_castle_base.png",
        mapX = 900f,
        mapY = 260f,
        blurb = "저주에 잠긴 고성 · 해골과 유령이 군림한다",
        places = listOf(
            Place(
                PlaceId.HOME, "야영지", "성문 앞 임시 천막",
                760f, 880f, 200f, 140f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
            ),
            Place(
                PlaceId.CHURCH, "폐예배당", "부서진 종탑",
                420f, 320f, 180f, 170f, BuildingStyle.CHURCH, 0xFF6A6A72, 0xFFC8C4B8
            ),
            Place(
                PlaceId.GRAY_CASTLE, "성채 입구", "고성의 심층 · Gray Keep",
                780f, 420f, 280f, 240f, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
            ),
            Place(
                PlaceId.BLACKSMITH, "버려진 무기고", "녹슨 갑옷이 쌓인 곳",
                1100f, 560f, 160f, 140f, BuildingStyle.ARMORY, 0xFF5A4132, 0xFF9B8266
            ),
        ),
        townsfolk = emptyList(),
        wellX = 700f,
        wellY = 620f,
    )

    /** 저주가 풀린 뒤 — 해방된 사람들이 사는 White Castle */
    private val whiteCastle: Settlement = Settlement(
        id = SettlementId.GRAY_CASTLE,
        nameKo = "하얀 성",
        nameEn = "White Castle",
        mapAsset = "white_castle_base.png",
        mapX = 900f,
        mapY = 260f,
        blurb = "저주에서 풀린 사람들 · 되살아난 성",
        places = listOf(
            Place(
                PlaceId.HOME, "왕실 숙소", "해방된 성의 거처",
                780f, 780f, 240f, 180f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC
            ),
            Place(
                PlaceId.CHURCH, "성당", "되찾은 예배당",
                480f, 300f, 200f, 190f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3
            ),
            Place(
                PlaceId.PUB, "연회장", "축제의 홀",
                320f, 520f, 220f, 180f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E
            ),
            Place(
                PlaceId.INN, "여관", "성안 여인숙",
                520f, 560f, 130f, 100f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4
            ),
            Place(
                PlaceId.SHOP, "시장", "성안 광장 노점",
                780f, 540f, 160f, 120f, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4
            ),
            Place(
                PlaceId.WEAPON_SHOP, "병기점", "왕실 병기",
                980f, 520f, 130f, 110f, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B
            ),
            Place(
                PlaceId.BLACKSMITH, "대장간", "성 대장간",
                1120f, 480f, 160f, 140f, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266
            ),
            Place(
                PlaceId.HOSPITAL, "의무실", "치유의 방",
                300f, 720f, 160f, 130f, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6
            ),
            Place(
                PlaceId.ARENA, "훈련장", "기사 훈련장",
                1100f, 780f, 170f, 130f, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C
            ),
            Place(
                PlaceId.MERCENARY, "기사단", "해방된 기사단 막사",
                1280f, 700f, 170f, 130f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668
            ),
            Place(
                PlaceId.MAGIC_SCHOOL, "마법당", "왕실 연구실",
                980f, 280f, 140f, 150f, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8
            ),
        ),
        townsfolk = listOf(
            Triple("farmer", 620f, 600f),
            Triple("merchant", 800f, 560f),
            Triple("shopkeeper", 740f, 540f),
            Triple("teacher", 1000f, 340f),
            Triple("chef", 360f, 560f),
            Triple("doctor", 340f, 760f),
            Triple("paladin", 1180f, 740f),
            Triple("warrior", 1080f, 800f),
        ),
        wellX = 760f,
        wellY = 500f,
    )

    fun all(castleCleared: Boolean = false): List<Settlement> =
        all(WorldFlags(castleCleared = castleCleared))

    fun all(flags: WorldFlags): List<Settlement> = listOf(
        oakhaven,
        ashbrook,
        castle(flags.castleCleared),
        igloo(flags.iglooCleared),
        seaside(flags.seasideCleared),
        winterCastle(flags.winterCleared),
    )

    fun castle(cleared: Boolean): Settlement =
        if (cleared) whiteCastle else grayCastle

    fun igloo(cleared: Boolean): Settlement =
        if (cleared) iglooThawed else iglooFrozen

    fun seaside(cleared: Boolean): Settlement =
        if (cleared) seasideRestored else seasideRuins

    fun winterCastle(cleared: Boolean): Settlement =
        if (cleared) winterRestored else winterCursed

    fun of(id: SettlementId, castleCleared: Boolean = false): Settlement =
        of(id, WorldFlags(castleCleared = castleCleared))

    fun of(id: SettlementId, flags: WorldFlags): Settlement = when (id) {
        SettlementId.OAKHAVEN -> oakhaven
        SettlementId.ASHBROOK -> ashbrook
        SettlementId.GRAY_CASTLE -> castle(flags.castleCleared)
        SettlementId.IGLOO -> igloo(flags.iglooCleared)
        SettlementId.SEASIDE -> seaside(flags.seasideCleared)
        SettlementId.WINTER_CASTLE -> winterCastle(flags.winterCleared)
    }

    private val restoredServices: List<Place> = listOf(
        Place(PlaceId.HOME, "거처", "되찾은 집", 780f, 780f, 240f, 180f, BuildingStyle.HOUSE, 0xFF9C4A34, 0xFFE8D4AC),
        Place(PlaceId.CHURCH, "예배당", "다시 울리는 종", 480f, 300f, 200f, 190f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3),
        Place(PlaceId.PUB, "선술집", "온기의 홀", 320f, 520f, 220f, 180f, BuildingStyle.PUB, 0xFF713B2A, 0xFFD0A66E),
        Place(PlaceId.INN, "여관", "따뜻한 잠자리", 520f, 560f, 130f, 100f, BuildingStyle.INN, 0xFF8A5A2B, 0xFFE3CFA4),
        Place(PlaceId.SHOP, "상점", "광장 노점", 780f, 540f, 160f, 120f, BuildingStyle.STORE, 0xFFB4573F, 0xFFEBD9B4),
        Place(PlaceId.WEAPON_SHOP, "무기점", "되찾은 병기", 980f, 520f, 130f, 110f, BuildingStyle.ARMORY, 0xFF6B3A2E, 0xFFD8C49B),
        Place(PlaceId.BLACKSMITH, "대장간", "모루의 불꽃", 1120f, 480f, 160f, 140f, BuildingStyle.FORGE, 0xFF5A4132, 0xFF9B8266),
        Place(PlaceId.HOSPITAL, "치료소", "치유의 집", 300f, 720f, 160f, 130f, BuildingStyle.CLINIC, 0xFFB0B6C4, 0xFFF2F0E6),
        Place(PlaceId.ARENA, "훈련장", "다시 열린 훈련장", 1100f, 780f, 170f, 130f, BuildingStyle.ARENA, 0xFF7A5230, 0xFFC9A87C),
        Place(PlaceId.MERCENARY, "용병소", "귀환한 용병들", 1280f, 700f, 170f, 130f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668),
        Place(PlaceId.MAGIC_SCHOOL, "연구실", "되찾은 탑", 980f, 280f, 140f, 150f, BuildingStyle.TOWER, 0xFF4B3B8F, 0xFFCFC7E8),
    )

    private val restoredFolk = listOf(
        Triple("farmer", 620f, 600f),
        Triple("merchant", 800f, 560f),
        Triple("shopkeeper", 740f, 540f),
        Triple("teacher", 1000f, 340f),
        Triple("chef", 360f, 560f),
        Triple("doctor", 340f, 760f),
        Triple("paladin", 1180f, 740f),
        Triple("warrior", 1080f, 800f),
    )

    /** 이글루 마을 — 얼음 별이 떨어진 뒤 얼어붙은 거점 */
    private val iglooFrozen: Settlement = Settlement(
        id = SettlementId.IGLOO,
        nameKo = "이글루 마을",
        nameEn = "Igloo Hamlet",
        mapAsset = "igloo_frozen.png",
        mapX = 320f,
        mapY = 180f,
        blurb = "한때 따뜻했던 북녘 · 얼음 별이 떨어지며 얼어붙었다",
        places = listOf(
            Place(PlaceId.HOME, "얼음 오두막", "눈 속에 파묻힌 거처", 420f, 780f, 200f, 150f, BuildingStyle.HOUSE, 0xFF6A90B0, 0xFFE0F0F8),
            Place(PlaceId.CHURCH, "얼음 사당", "얼어붙은 기도처", 300f, 300f, 180f, 170f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3),
            Place(
                PlaceId.IGLOO_GLACIER, "빙하지대", "지하 20층의 얼음북극곰 · Glacier",
                1080f, 340f, 260f, 200f, BuildingStyle.GLACIER, 0xFF6A90B0, 0xFFE0F0F8
            ),
            Place(PlaceId.BLACKSMITH, "언 대장간", "꺼진 모루", 720f, 560f, 160f, 140f, BuildingStyle.ARMORY, 0xFF5A4132, 0xFF9B8266),
        ),
        townsfolk = emptyList(),
        wellX = 640f,
        wellY = 600f,
    )

    private val iglooThawed: Settlement = Settlement(
        id = SettlementId.IGLOO,
        nameKo = "이글루 마을",
        nameEn = "Thawed Igloo",
        mapAsset = "igloo_thawed.png",
        mapX = 320f,
        mapY = 180f,
        blurb = "얼음 별의 추위가 걷힌 북녘 · 온기가 돌아왔다",
        places = restoredServices,
        townsfolk = restoredFolk,
        wellX = 760f,
        wellY = 500f,
    )

    /** 바닷가 폐허 — 대왕문어의 해일로 잠긴 마을 */
    private val seasideRuins: Settlement = Settlement(
        id = SettlementId.SEASIDE,
        nameKo = "바닷가 폐허",
        nameEn = "Seaside Ruins",
        mapAsset = "seaside_ruins.png",
        mapX = 280f,
        mapY = 820f,
        blurb = "해일에 잠긴 어촌 · 대왕문어가 바다를 뒤흔든다",
        places = listOf(
            Place(PlaceId.HOME, "침수된 집", "물기 찬 임시 거처", 760f, 820f, 200f, 150f, BuildingStyle.HOUSE, 0xFF3A6B8C, 0xFFC8D8E0),
            Place(PlaceId.CHURCH, "침수 예배당", "종탑만 물 위에 남았다", 380f, 340f, 180f, 170f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3),
            Place(
                PlaceId.SEA_CAVE, "바다 동굴", "지하 20층의 대왕문어 · Sea Cave",
                1180f, 460f, 260f, 210f, BuildingStyle.CAVE, 0xFF2A4A5A, 0xFF4A6A78
            ),
            Place(PlaceId.BLACKSMITH, "녹슨 공방", "소금에 잠긴 모루", 920f, 640f, 160f, 140f, BuildingStyle.ARMORY, 0xFF5A4132, 0xFF9B8266),
        ),
        townsfolk = emptyList(),
        wellX = 700f,
        wellY = 620f,
    )

    private val seasideRestored: Settlement = Settlement(
        id = SettlementId.SEASIDE,
        nameKo = "바닷가 마을",
        nameEn = "Seaside Haven",
        mapAsset = "seaside_restored.png",
        mapX = 280f,
        mapY = 820f,
        blurb = "해일이 걷힌 어촌 · 배가 다시 항구에 닿는다",
        places = restoredServices,
        townsfolk = restoredFolk,
        wellX = 760f,
        wellY = 500f,
    )

    /** 겨울성 — 아이들이 납치된 뒤 영원한 겨울이 내린 성 */
    private val winterCursed: Settlement = Settlement(
        id = SettlementId.WINTER_CASTLE,
        nameKo = "겨울성",
        nameEn = "Winter Keep",
        mapAsset = "winter_cursed.png",
        mapX = 1100f,
        mapY = 180f,
        blurb = "아이들이 사라진 성 · 납치와 함께 겨울이 내렸다",
        places = listOf(
            Place(PlaceId.HOME, "성문 천막", "눈보라 속 임시 야영", 760f, 880f, 200f, 140f, BuildingStyle.CAMP, 0xFF4E5A3A, 0xFF8B9668),
            Place(PlaceId.CHURCH, "얼어붙은 성당", "종소리 없는 예배당", 420f, 320f, 180f, 170f, BuildingStyle.CHURCH, 0xFF8C8FA6, 0xFFE6E1D3),
            Place(
                PlaceId.WINTER_KEEP, "지하 던전", "납치범 두목 · Winter Dungeon",
                780f, 420f, 280f, 240f, BuildingStyle.CAVE, 0xFF3B3630, 0xFF56504A
            ),
            Place(PlaceId.BLACKSMITH, "버려진 무기고", "녹슨 겨울 병기", 1100f, 560f, 160f, 140f, BuildingStyle.ARMORY, 0xFF5A4132, 0xFF9B8266),
        ),
        townsfolk = emptyList(),
        wellX = 700f,
        wellY = 620f,
    )

    private val winterRestored: Settlement = Settlement(
        id = SettlementId.WINTER_CASTLE,
        nameKo = "겨울성",
        nameEn = "Restored Keep",
        mapAsset = "winter_restored.png",
        mapX = 1100f,
        mapY = 180f,
        blurb = "아이들이 돌아온 성 · 봄이 성벽에 스민다",
        places = restoredServices,
        townsfolk = restoredFolk,
        wellX = 760f,
        wellY = 500f,
    )
}
