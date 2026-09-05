package com.medieval.village.model

enum class InteriorNpcKind {
    KEEPER,   // 주인/점원 — 다가가면 거래 메뉴
    HELPER,   // 조수
    VISITOR   // 손님·여행객 — 대화만
}

data class InteriorNpc(
    val id: String,
    val placeId: PlaceId,
    val name: String,
    val role: String,
    val kind: InteriorNpcKind,
    /** 실내 캔버스 상대 좌표 (0~1) — 월드 좌표로도 변환한다. */
    val fx: Float,
    val fy: Float,
    val lines: List<String>,
    /** 커스텀 스프라이트 키 (없으면 장소·역할 기본값) */
    val spriteKey: String? = null,
) {
    val worldX: Float get() = fx * InteriorRoom.WORLD_W
    val worldY: Float get() = fy * InteriorRoom.WORLD_H
}

object InteriorNpcCatalog {

    fun forPlace(
        placeId: PlaceId,
        settlementId: SettlementId = SettlementId.OAKHAVEN,
        flags: WorldFlags = WorldFlags(),
    ): List<InteriorNpc> = all.filter { it.placeId == placeId }.map { npc ->
        val lines = RegionDialogue.interiorLines(settlementId, flags, npc)
        if (lines === npc.lines) npc else npc.copy(lines = lines)
    }

    val all: List<InteriorNpc> = listOf(
        // ----- 집 -----
        npc(PlaceId.HOME, "home_sister", "미라", "동생", InteriorNpcKind.HELPER, 0.72f, 0.78f, "farmer",
            "오빠, 오늘은 조심해서 다녀와.", "창밖에서 또 새가 울어.", "좀비석 얘기는… 무서워."),
        npc(PlaceId.HOME, "home_neighbor", "올가", "이웃", InteriorNpcKind.VISITOR, 0.32f, 0.62f, "merchant",
            "문단속은 하고 다니지? 밤마다 하수도 소리가 들려.", "미라 잘 챙기고. 요즘 애들 걱정이야.", "포도주 향이 그리운 시절이었어."),

        // ----- 잡화 -----
        npc(PlaceId.SHOP, "shop_keeper", "한나", "잡화 주인", InteriorNpcKind.KEEPER, 0.72f, 0.58f, "shopkeeper",
            "어서 와요! 횃불이랑 붕대 챙기셨나요?", "오늘 빵은 따끈해요.", "던전 가시면 물약은 필수예요."),
        npc(PlaceId.SHOP, "shop_boy", "펠", "점원", InteriorNpcKind.HELPER, 0.86f, 0.72f, "merchant",
            "손님, 이쪽 선반 보세요!", "재고는 매일 아침 채워요.", "안녕하세요!"),
        npc(PlaceId.SHOP, "shop_traveler", "카렌", "여행객", InteriorNpcKind.VISITOR, 0.28f, 0.68f, "rogue",
            "횃불 값이 또 올랐군… 그래도 사야지.", "지하 1층만 다녀와도 좀비가 바글바글이야.", "오크헤이븐에서 하룻밤 묵고 동쪽로 간다."),
        npc(PlaceId.SHOP, "shop_buyer", "톰", "동네 손님", InteriorNpcKind.VISITOR, 0.42f, 0.78f, "farmer",
            "빵 두 개만… 아, 물약도 하나.", "아이들 몫이라 자주 들러요.", "한나 아주머니 물건은 믿을 만하지."),

        // ----- 무기점 -----
        npc(PlaceId.WEAPON_SHOP, "weapon_smith", "가렌", "무기상", InteriorNpcKind.KEEPER, 0.70f, 0.58f, "blacksmith",
            "좋은 쇠를 찾는군.", "좀비 뼈도 가르려면 날이 살아야지.", "방패부터 챙기는 게 현명해."),
        npc(PlaceId.WEAPON_SHOP, "weapon_buyer", "릭", "모험가", InteriorNpcKind.VISITOR, 0.30f, 0.70f, "warrior",
            "검이 무뎌져서… 교체해야겠어.", "갑옷 좀비는 방패 없으면 힘들더군.", "가렌 대장 물건은 비싸도 값한다."),
        npc(PlaceId.WEAPON_SHOP, "weapon_scout", "니엘", "정찰병", InteriorNpcKind.VISITOR, 0.48f, 0.80f, "rogue",
            "단검만 있어도 하수도에선 목숨을 건진다.", "심층은 아직… 장비부터 모아야지.", "요새 밤마다 동굴 쪽에서 울음소리가 들려."),

        // ----- 병원 -----
        npc(PlaceId.HOSPITAL, "doc", "엘라", "의사", InteriorNpcKind.KEEPER, 0.68f, 0.56f, "doctor",
            "어디가 아파서 오셨습니까?", "물린 상처는 바로 소독해야 해요.", "해독 영양제도 준비해 두었어요."),
        npc(PlaceId.HOSPITAL, "nurse", "니나", "간호", InteriorNpcKind.HELPER, 0.84f, 0.70f, "teacher",
            "침대는 이쪽이에요.", "깊게 숨 쉬어 보세요.", "회복을 빌어요."),
        npc(PlaceId.HOSPITAL, "hosp_patient", "베노", "환자", InteriorNpcKind.VISITOR, 0.28f, 0.72f, "farmer",
            "하수도에서 발목을… 으.", "엘라 선생님 덕에 살았어.", "좀비 이빨은 독보다 무섭다니까."),
        npc(PlaceId.HOSPITAL, "hosp_visitor", "사라", "면회객", InteriorNpcKind.VISITOR, 0.45f, 0.82f, "merchant",
            "남편이 던전에서 다쳤어요…", "해독약이 더 있으면 좋겠어요.", "마을 사람들이 서로 돕지 않으면 버틸 수 없어요."),

        // ----- 교회 -----
        npc(PlaceId.CHURCH, "priest", "마르코", "사제", InteriorNpcKind.KEEPER, 0.70f, 0.56f, "mage",
            "빛의 가호가 함께하기를.", "저주를 씻는 기도를 올리겠소.", "헌금은 마을을 지키는 불빛이오."),
        npc(PlaceId.CHURCH, "nun", "세라", "수녀", InteriorNpcKind.HELPER, 0.86f, 0.72f, "paladin",
            "편히 기도하세요.", "마음이 무겁다면 말씀해 주세요.", "평안을 빕니다."),
        npc(PlaceId.CHURCH, "church_pilgrim", "요한", "순례자", InteriorNpcKind.VISITOR, 0.30f, 0.74f, "farmer",
            "좀비석 때문에 길을 잃을 뻔했소.", "기도만이 마음을 단단히 해 주오.", "영주의 욕망이 이 비극을 불렀다더군."),
        npc(PlaceId.CHURCH, "church_widow", "마야", "신자", InteriorNpcKind.VISITOR, 0.48f, 0.82f, "merchant",
            "남편을 지하에… 잃은 뒤론 여기만 옵니다.", "축복이 조금이라도 닿기를.", "감사합니다, 여행자여."),

        // ----- 여관 -----
        npc(PlaceId.INN, "innkeep", "롤프", "여관 주인", InteriorNpcKind.KEEPER, 0.70f, 0.56f, "merchant",
            "방 하나 잡으시겠어요?", "문은 꼭 잠그세요.", "밤에 하수도 기척이 들린다오."),
        npc(PlaceId.INN, "maid", "루나", "하녀", InteriorNpcKind.HELPER, 0.86f, 0.68f, "chef",
            "이불은 따뜻하게 갈아두었어요.", "소문 들으러 오신 건가요?", "편히 쉬세요."),
        npc(PlaceId.INN, "inn_traveler", "드레이크", "여행 상인", InteriorNpcKind.VISITOR, 0.28f, 0.64f, "merchant",
            "남쪽 길도 막혔다더군. 여기 묵는 수밖에.", "좀비석 소문이 다른 마을까지 퍼졌어.", "따뜻한 스튜 한 그릇이 목숨을 살린다."),
        npc(PlaceId.INN, "inn_hunter", "프레이", "사냥꾼", InteriorNpcKind.VISITOR, 0.42f, 0.76f, "rogue",
            "숲에서도 이상한 발자국이 보여.", "밤은 여관이 제일 안전하지.", "내일 새벽엔 다시 길을 나선다."),
        npc(PlaceId.INN, "inn_bard", "리라", "음유시인", InteriorNpcKind.VISITOR, 0.55f, 0.70f, "mage",
            "풍요의 마을을 노래하려 왔더니… 곡조가 어두워졌어.", "포도주 대신 눈물 소리가 들리네.", "한 곡 들려줄까? 대가를 바라진 않아."),

        // ----- 대련소 -----
        npc(PlaceId.ARENA, "coach", "브룬", "교관", InteriorNpcKind.KEEPER, 0.70f, 0.56f, "warrior",
            "몸 좀 풀러 왔나?", "지상에서라도 칼날을 갈아야지.", "이기면 상금이다!"),
        npc(PlaceId.ARENA, "arena_spar", "걸", "수련생", InteriorNpcKind.VISITOR, 0.30f, 0.72f, "warrior",
            "브룬 교관한테 또 깨졌어…", "던전 가기 전엔 여기서 몸을 풀어.", "한 판 붙자! …농담이야."),
        npc(PlaceId.ARENA, "arena_vet", "휴고", "퇴역 병사", InteriorNpcKind.VISITOR, 0.48f, 0.80f, "paladin",
            "예전에 영주 경호대였지. 이젠 목검만 든다.", "심층은 젊은이들이 가는 곳이야.", "방패를 낮추면 끝장이다."),

        // ----- 대장간 -----
        npc(PlaceId.BLACKSMITH, "forge", "드루", "대장장이", InteriorNpcKind.KEEPER, 0.68f, 0.58f, "blacksmith",
            "쇠는 두들길수록 강해지지.", "좀비 이빨에 안 깨지려면 더 달궈야 해.", "강화는 신중히."),
        npc(PlaceId.BLACKSMITH, "forge_apprentice", "코브", "견습", InteriorNpcKind.HELPER, 0.84f, 0.74f, "farmer",
            "망치가 너무 무거워요…", "불씨를 조심하세요!", "오늘도 강화 의뢰가 많아요."),
        npc(PlaceId.BLACKSMITH, "forge_customer", "에단", "용병", InteriorNpcKind.VISITOR, 0.32f, 0.72f, "warrior",
            "칼날이 금이 갔어. 드루한테 맡긴다.", "강화 한 번이면 심층도 버틸까…", "불꽃 냄새는 여전해."),

        // ----- 마법학교 -----
        npc(PlaceId.MAGIC_SCHOOL, "mage", "이리스", "마법사", InteriorNpcKind.KEEPER, 0.70f, 0.56f, "mage",
            "해독 연구에 오신 건가.", "마법은 반복일세.", "좀비석 기운은 함부로 만지지 말게."),
        npc(PlaceId.MAGIC_SCHOOL, "apprentice", "얀", "견습", InteriorNpcKind.HELPER, 0.86f, 0.72f, "teacher",
            "고서가 또 날아갔어요…", "화염구 수업은 오후예요.", "안녕하세요!"),
        npc(PlaceId.MAGIC_SCHOOL, "magic_scholar", "셀레", "연구생", InteriorNpcKind.VISITOR, 0.30f, 0.70f, "mage",
            "좀비석 조각의 파동을 기록 중이야.", "해독 주문은 아직 불완전해.", "호기심이 목숨을 위협하지… 조심해."),
        npc(PlaceId.MAGIC_SCHOOL, "magic_guest", "브린", "방문 학자", InteriorNpcKind.VISITOR, 0.48f, 0.80f, "merchant",
            "타 마을에서도 비슷한 저주가 시작됐어.", "이리스의 연구 노트를 빌리러 왔지.", "정화 마법만이 희망이다."),

        // ----- 용병고용소 -----
        npc(PlaceId.MERCENARY, "recruiter", "오스카", "용병대장", InteriorNpcKind.KEEPER, 0.68f, 0.56f, "warrior",
            "돈만 주면 붙여주지.", "좀비 둥지 안내라면 목숨값은 별도야.", "원정대는 둘이면 충분해."),
        npc(PlaceId.MERCENARY, "rookie", "팀", "신입 용병", InteriorNpcKind.VISITOR, 0.84f, 0.74f, "rogue",
            "저도 곧 나갈 거예요!", "칼 좀 빌려주시겠어요…?", "파이팅!"),
        npc(PlaceId.MERCENARY, "merc_vet", "로안", "베테랑", InteriorNpcKind.VISITOR, 0.30f, 0.68f, "paladin",
            "심층까지 갔다 온 사람은 7명뿐이야. 살아남은 건 3명.", "동료를 고를 땐 실력보다 신뢰를 봐.", "오스카 밑에서 일하면 밥은 굶지 않는다."),
        npc(PlaceId.MERCENARY, "merc_hire", "핀", "구직 모험가", InteriorNpcKind.VISITOR, 0.48f, 0.80f, "warrior",
            "원정대에 끼워줄 사람 없나…", "혼자서는 지하 2층도 버거워.", "자네 파티에 빈자리 있으면 말해."),
    )

    private fun npc(
        placeId: PlaceId,
        id: String,
        name: String,
        role: String,
        kind: InteriorNpcKind,
        fx: Float,
        fy: Float,
        spriteKey: String?,
        vararg lines: String
    ) = InteriorNpc(id, placeId, name, role, kind, fx, fy, lines.toList(), spriteKey)
}
