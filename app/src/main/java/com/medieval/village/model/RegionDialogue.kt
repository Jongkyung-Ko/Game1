package com.medieval.village.model

/**
 * 정착지(오크헤이븐·애쉬브룩·회색/하얀 성)별 NPC·입장 대사.
 * 성 해방 이후에는 저주 해방·전설 성취 스토리로 전환한다.
 */
object RegionDialogue {

    fun interiorLines(
        settlementId: SettlementId,
        castleCleared: Boolean,
        npc: InteriorNpc,
    ): List<String> = when (settlementId) {
        SettlementId.ASHBROOK -> ashbrookInterior(npc)
        SettlementId.GRAY_CASTLE ->
            if (castleCleared) whiteCastleInterior(npc) else grayCastleInterior(npc)
        SettlementId.OAKHAVEN -> npc.lines
    }

    fun pubNpcs(settlementId: SettlementId, castleCleared: Boolean): List<PubNpc> =
        when (settlementId) {
            SettlementId.ASHBROOK -> ashbrookPub
            SettlementId.GRAY_CASTLE ->
                if (castleCleared) whiteCastlePub else grayCastlePub
            SettlementId.OAKHAVEN -> PubNpcCatalog.oakhavenBase
        }

    fun placeGreeting(
        settlementId: SettlementId,
        castleCleared: Boolean,
        id: PlaceId,
    ): String? = when (settlementId) {
        SettlementId.ASHBROOK -> ashbrookGreeting(id)
        SettlementId.GRAY_CASTLE ->
            if (castleCleared) whiteCastleGreeting(id) else grayCastleGreeting(id)
        SettlementId.OAKHAVEN -> null
    }

    // ----- Ashbrook -----

    private fun ashbrookInterior(npc: InteriorNpc): List<String> = when (npc.id) {
        "home_sister" -> listOf(
            "오빠, 강물 소리가 더 커진 것 같아.",
            "애쉬브룩 저녁 연기는 따뜻해.",
            "오크헤이븐보다 여기가 덜 무서울까…?",
        )
        "home_neighbor" -> listOf(
            "강 안개 낀 아침엔 문단속을 해.",
            "재 마을이라 부르지만, 마음은 아직 따뜻하단다.",
            "부두 쪽 물고기가 요즘 잘 잡혀.",
        )
        "shop_keeper" -> listOf(
            "어서 와요! 강변 잡화는 여기가 제일이에요.",
            "횃불은 절벽 동굴 갈 때 필수예요.",
            "애쉬브룩 빵은 소금기가 살짝 있어요.",
        )
        "shop_boy" -> listOf(
            "손님, 강가 쪽 선반 보세요!",
            "재고는 배로 실어 와요.",
            "안녕하세요, 여행자님!",
        )
        "shop_traveler" -> listOf(
            "오크헤이븐에서 넘어왔소. 공기가 다르군.",
            "절벽 동굴 소문이 여기까지 왔더군.",
            "강길을 따라 남쪽으로 가려 하네.",
        )
        "shop_buyer" -> listOf(
            "빵이랑… 강물용 물약도 하나.",
            "아이들과 부두에서 놀다 자주 들러요.",
            "한나 아주머니 물건은 믿을 만하지.",
        )
        "weapon_smith" -> listOf(
            "강바람에도 녹슬지 않는 날을 찾나.",
            "절벽 짐승은 날이 살아야 해.",
            "방패부터 챙기는 게 현명해.",
        )
        "weapon_buyer" -> listOf(
            "검이 무뎌져서… 교체해야겠어.",
            "동굴 박쥐는 방패가 없으면 힘들더군.",
            "가렌 대장 물건은 비싸도 값한다.",
        )
        "weapon_scout" -> listOf(
            "단검만 있어도 절벽길에선 목숨을 건진다.",
            "심층은 아직… 장비부터 모아야지.",
            "밤에 강 너머에서 울음소리가 들려.",
        )
        "doc" -> listOf(
            "어디가 아파서 오셨습니까?",
            "강가 미끄럼 상처도 바로 소독해야 해요.",
            "해독 영양제도 준비해 두었어요.",
        )
        "nurse" -> listOf(
            "침대는 이쪽이에요.",
            "깊게 숨 쉬어 보세요.",
            "애쉬브룩의 평안을 빌어요.",
        )
        "hosp_patient" -> listOf(
            "절벽에서 발목을… 으.",
            "엘라 선생님 덕에 살았어.",
            "강바람보다 동굴 이빨이 무섭다니까.",
        )
        "hosp_visitor" -> listOf(
            "남편이 동굴에서 다쳤어요…",
            "해독약이 더 있으면 좋겠어요.",
            "강변 사람들이 서로 돕지 않으면 버틸 수 없어요.",
        )
        "priest" -> listOf(
            "강의 가호가 함께하기를.",
            "푸른 지붕 아래에서 기도를 올리겠소.",
            "헌금은 마을을 지키는 불빛이오.",
        )
        "nun" -> listOf(
            "편히 기도하세요.",
            "마음이 무겁다면 말씀해 주세요.",
            "강처럼 고요한 평안을 빕니다.",
        )
        "church_pilgrim" -> listOf(
            "오크헤이븐 저주 때문에 길을 잃을 뻔했소.",
            "기도만이 마음을 단단히 해 주오.",
            "애쉬브룩은 아직 숨이 트이는 곳이오.",
        )
        "church_widow" -> listOf(
            "남편을 절벽에서… 잃은 뒤론 여기만 옵니다.",
            "축복이 조금이라도 닿기를.",
            "감사합니다, 여행자여.",
        )
        "innkeep" -> listOf(
            "방 하나 잡으시겠어요? 부두 전망이 좋아요.",
            "문은 꼭 잠그세요. 강안개가 짙을 때요.",
            "밤에 절벽 쪽에서 기척이 들린다오.",
        )
        "maid" -> listOf(
            "이불은 따뜻하게 갈아두었어요.",
            "강 소문 들으러 오신 건가요?",
            "편히 쉬세요.",
        )
        "inn_traveler" -> listOf(
            "남쪽 평원 길도 먼지투성이라더군.",
            "오크헤이븐 소문이 여기까지 퍼졌어.",
            "따뜻한 스튜 한 그릇이 목숨을 살린다.",
        )
        "inn_hunter" -> listOf(
            "절벽 숲에서도 이상한 발자국이 보여.",
            "밤은 여관이 제일 안전하지.",
            "내일 새벽엔 다시 강길을 나선다.",
        )
        "inn_bard" -> listOf(
            "재 마을의 노래를 부르려 왔더니…",
            "강물 소리에 맞춰 줄을 고쳐야겠어.",
            "한 곡 들려줄까? 대가를 바라진 않아.",
        )
        "coach" -> listOf(
            "몸 좀 풀러 왔나? 강바람 맞으며 수련하지.",
            "지상에서라도 칼날을 갈아야지.",
            "이기면 상금이다!",
        )
        "arena_spar" -> listOf(
            "브룬 교관한테 또 깨졌어…",
            "동굴 가기 전엔 여기서 몸을 풀어.",
            "한 판 붙자! …농담이야.",
        )
        "arena_vet" -> listOf(
            "예전에 강 순찰대였지. 이젠 목검만 든다.",
            "심층은 젊은이들이 가는 곳이야.",
            "방패를 낮추면 끝장이다.",
        )
        "forge" -> listOf(
            "쇠는 두들길수록 강해지지.",
            "강물로 달군 날을 식히면 더 단단해져.",
            "강화는 신중히.",
        )
        "forge_apprentice" -> listOf(
            "망치가 너무 무거워요…",
            "불씨를 조심하세요!",
            "오늘도 강화 의뢰가 많아요.",
        )
        "forge_customer" -> listOf(
            "칼날이 금이 갔어. 드루한테 맡긴다.",
            "강화 한 번이면 절벽 동굴도 버틸까…",
            "불꽃 냄새는 여전해.",
        )
        "mage" -> listOf(
            "강안개 마력 연구에 오신 건가.",
            "마법은 반복일세.",
            "오크헤이븐 쪽 저주 기운은 함부로 만지지 말게.",
        )
        "apprentice" -> listOf(
            "고서가 또 날아갔어요…",
            "화염구 수업은 오후예요.",
            "안녕하세요!",
        )
        "magic_scholar" -> listOf(
            "강물 파동을 기록 중이야.",
            "해독 주문은 아직 불완전해.",
            "호기심이 목숨을 위협하지… 조심해.",
        )
        "magic_guest" -> listOf(
            "타 마을에서도 비슷한 소문이 들려.",
            "이리스의 연구 노트를 빌리러 왔지.",
            "정화 마법만이 희망이다.",
        )
        "recruiter" -> listOf(
            "돈만 주면 붙여주지. 강변 용병은 값이 세.",
            "절벽 동굴 안내라면 목숨값은 별도야.",
            "원정대는 둘이면 충분해.",
        )
        "rookie" -> listOf(
            "저도 곧 나갈 거예요!",
            "칼 좀 빌려주시겠어요…?",
            "파이팅!",
        )
        "merc_vet" -> listOf(
            "절벽 심층까지 갔다 온 사람은 드물어.",
            "동료를 고를 땐 실력보다 신뢰를 봐.",
            "오스카 밑에서 일하면 밥은 굶지 않는다.",
        )
        "merc_hire" -> listOf(
            "원정대에 끼워줄 사람 없나…",
            "혼자서는 동굴 2층도 버거워.",
            "자네 파티에 빈자리 있으면 말해.",
        )
        else -> npc.lines
    }

    private fun ashbrookGreeting(id: PlaceId): String? = when (id) {
        PlaceId.HOME -> "강물 소리가 창문을 두드린다. 애쉬브룩의 농가가 맞아준다."
        PlaceId.SHOP -> "\"어서 오세요… 강변 잡화는 늘 비치해 둡니다.\""
        PlaceId.WEAPON_SHOP -> "\"절벽 짐승이라도 가를 쇠를 찾나? 잘 왔네.\""
        PlaceId.HOSPITAL -> "\"강가 상처입니까, 아니면 동굴 기운입니까?\""
        PlaceId.CHURCH -> "\"푸른 지붕 아래, 강의 가호가 그대와 함께하기를.\""
        PlaceId.INN -> "\"문은 꼭 잠그세요. 밤엔 절벽 쪽에서 기척이 들립니다.\""
        PlaceId.PUB -> "강물 향 사이로, 재 마을의 소문이 낮게 섞여 들린다."
        PlaceId.ARENA -> "\"강바람 맞으며 칼날을 갈아야지.\""
        PlaceId.DUNGEON -> "절벽 동굴의 찬 바람이 얼굴을 스친다."
        PlaceId.EAST_FOREST -> "절벽 너머 숲길이 열린다. 짐승의 울음이 멀지 않다."
        PlaceId.SOUTH_DESERT -> "평원으로 이어지는 먼지길이 남쪽으로 뻗는다."
        PlaceId.NORTH_GLACIER -> "산록으로 가는 언덕바람이 차갑다."
        PlaceId.BLACKSMITH -> "\"강물로 달군 쇠는 더 단단하지.\""
        PlaceId.MAGIC_SCHOOL -> "\"강안개의 마력… 우리는 조심스럽게 연구한다네.\""
        PlaceId.MERCENARY -> "\"절벽 안내라면 돈만 주면 붙여주지. 목숨값은 별도야.\""
        else -> null
    }

    private val ashbrookPub = listOf(
        PubNpc(
            id = "owner", name = "보릭", role = "주인장", kind = NpcKind.OWNER,
            x = 805f, y = 390f, spriteKey = "merchant",
            lines = listOf(
                "강변의 재에 잘 왔네. 배가 닿을 때마다 소문이 쌓이지.",
                "오크헤이븐 좀비석 얘기는 여기 손님들 사이에서도 끊이질 않아.",
                "절벽 동굴 갈 거면 횃불이랑 물약부터 챙기게.",
            )
        ),
        PubNpc(
            id = "traveler", name = "엘린", role = "여행객", kind = NpcKind.TRAVELER,
            x = 335f, y = 245f, spriteKey = "rogue",
            lines = listOf(
                "재 마을이라니… 강안개가 예뻐요.",
                "오크헤이븐보다 숨이 트이네요.",
                "부두에서 하룻밤 묵고 동쪽 숲으로 가려 해요.",
            )
        ),
        PubNpc(
            id = "guild", name = "케인", role = "길드 멤버", kind = NpcKind.GUILD_MEMBER,
            x = 565f, y = 345f, spriteKey = "warrior",
            lines = listOf(
                "절벽 동굴도 직접 걸어 다니며 싸워야 해.",
                "강길 용병은 값이 세지만 믿을 만하지.",
                "Status에서 용병 둘을 원정대로 뽑아.",
            )
        ),
        PubNpc(
            id = "drunk", name = "토드", role = "취객", kind = NpcKind.DRUNK,
            x = 210f, y = 505f, spriteKey = "farmer",
            lines = listOf(
                "히끅… 강물이… 재처럼 흐려 보여…",
                "오크헤이븐 놈들… 돌을 만지다가 마을을 망쳤지!",
                "최하층에 뭔가 있다던데… 만지면… 히끅… 끝장이야…",
            )
        ),
    )

    // ----- Gray Castle (cursed) -----

    private fun grayCastleInterior(npc: InteriorNpc): List<String> = when (npc.placeId) {
        PlaceId.HOME -> listOf(
            "천막 안에서조차 해골의 발소리가 들린다.",
            "성문 앞이 그나마 안전하다… 아직은.",
            "저주가 풀리기 전엔 여기 머물 수밖에 없다.",
        )
        PlaceId.CHURCH -> listOf(
            "부서진 종탑… 기도조차 메아리치지 않소.",
            "해골 왕이 성채 꼭대기에 군림한다더군.",
            "빛이여, 이 저주를 끊어 주소서…",
        )
        PlaceId.BLACKSMITH -> listOf(
            "녹슨 갑옷만 가득하다. 산 자의 무기는 드물어.",
            "성채 심층에 들어가려면 날이 살아야 해.",
            "언데드의 뼈도 가를 쇠를 찾아라.",
        )
        else -> listOf(
            "회색 돌벽이 숨 막히게 다가온다.",
            "저주가 돌에 스며 있다… 조심해라.",
            "해골과 유령이 성채를 지킨다.",
        )
    }

    private fun grayCastleGreeting(id: PlaceId): String? = when (id) {
        PlaceId.HOME -> "성문 앞 천막. 저주의 바람이 천을 흔든다."
        PlaceId.CHURCH -> "폐예배당의 침묵이 무겁다. 종은 이미 부서졌다."
        PlaceId.BLACKSMITH -> "버려진 무기고. 녹과 먼지만 남았다."
        PlaceId.GRAY_CASTLE -> "회색 돌문이 열린다. 해골과 유령의 숨결이 심층에서 흘러나온다."
        else -> "저주에 잠긴 고성… 발소리조차 조심스럽다."
    }

    private val grayCastlePub = listOf(
        PubNpc(
            id = "owner", name = "유령 주점지기", role = "환영", kind = NpcKind.OWNER,
            x = 805f, y = 390f, spriteKey = "merchant",
            lines = listOf(
                "…산 자가 여기까지 오다니.",
                "성채 10층에 해골 왕이 있다…",
                "저주가 풀리기 전엔 잔도 차갑다.",
            )
        ),
        PubNpc(
            id = "traveler", name = "떠도는 혼", role = "망령", kind = NpcKind.TRAVELER,
            x = 335f, y = 245f, spriteKey = "rogue",
            lines = listOf(
                "나도… 예전에 용사였다…",
                "왕을 쓰러뜨려… 우리를 풀어줘…",
                "돌문이… 열린다…",
            )
        ),
        PubNpc(
            id = "guild", name = "쓰러진 기사", role = "유골", kind = NpcKind.GUILD_MEMBER,
            x = 565f, y = 345f, spriteKey = "warrior",
            lines = listOf(
                "방패를… 낮추지 마라…",
                "유령기마병은… 빠르다…",
                "10층의 왕이… 열쇠다…",
            )
        ),
        PubNpc(
            id = "drunk", name = "저주받은 취객", role = "망자", kind = NpcKind.DRUNK,
            x = 210f, y = 505f, spriteKey = "farmer",
            lines = listOf(
                "히끅… 포도주가… 재가 됐지…",
                "왕이… 우리를 붙잡았다…",
                "전설의 용사여… 와 다오…",
            )
        ),
    )

    // ----- White Castle (liberated) -----

    private fun whiteCastleInterior(npc: InteriorNpc): List<String> = when (npc.id) {
        "home_sister", "home_neighbor" -> listOf(
            "용사가 저주를 풀어 주셨다… 고맙습니다!",
            "하얀 성에서 다시 숨을 쉴 수 있어요.",
            "전설이 이뤄졌어요. 당신 덕분이에요.",
        )
        "shop_keeper", "shop_boy" -> listOf(
            "해방된 시장에 어서 오세요!",
            "용사님께 감사의 빵을 드려요.",
            "저주가 걷히니 물건도 빛나요.",
        )
        "shop_traveler", "shop_buyer" -> listOf(
            "전설의 용사를 뵙다니…!",
            "회색 성이 하얗게 되다니, 믿기지 않아요.",
            "당신 덕분에 아이들이 웃게 됐어요.",
        )
        "weapon_smith", "weapon_buyer", "weapon_scout" -> listOf(
            "해방의 칼날을 기리겠소.",
            "해골 왕을 쓰러뜨린 분이시군요!",
            "병기는 이제 축제를 위해 빛내겠소.",
        )
        "doc", "nurse", "hosp_patient", "hosp_visitor" -> listOf(
            "저주의 상처가 아물어 갑니다.",
            "용사님, 고맙습니다. 전설이 현실이 됐군요.",
            "의무실에 다시 온기가 돌아요.",
        )
        "priest", "nun", "church_pilgrim", "church_widow" -> listOf(
            "빛이여, 용사의 길을 축복하소서!",
            "저주가 풀렸소. 당신이 전설을 이뤄 주었소.",
            "성당 종이 다시 울립니다. 감사합니다.",
        )
        "innkeep", "maid", "inn_traveler", "inn_hunter", "inn_bard" -> listOf(
            "해방 축제의 방이 준비돼 있어요!",
            "용사님, 편히 쉬세요. 성은 당신 덕에 살았소.",
            "노래가 다시 밝아졌어요. 전설이 이뤄졌죠.",
        )
        "coach", "arena_spar", "arena_vet" -> listOf(
            "기사 훈련장이 다시 열렸소!",
            "용사의 검술을 기리며 수련한다!",
            "저주가 걷히니 칼날도 가볍군.",
        )
        "forge", "forge_apprentice", "forge_customer" -> listOf(
            "해방의 쇠를 두드리겠소!",
            "용사님 검에 감사를 담아 날을 갈겠소.",
            "성이 되살아나니 모루도 웃는다오.",
        )
        "mage", "apprentice", "magic_scholar", "magic_guest" -> listOf(
            "저주의 파동이 사라졌네. 당신이 끊었지.",
            "전설이 이뤄졌다… 기록에 남기겠네.",
            "정화의 빛이 성 전체를 감쌌어. 고맙네.",
        )
        "recruiter", "rookie", "merc_vet", "merc_hire" -> listOf(
            "해방된 기사단에 잘 왔네, 용사여!",
            "당신 덕분에 우리가 사람으로 돌아왔소.",
            "전설을 이룬 분과 어깨를 나란히 하다니…!",
        )
        else -> listOf(
            "용사가 저주를 풀어 주었다. 고맙다!",
            "전설이 이뤄졌다… 하얀 성이 되살아났다.",
            "당신 덕분에 우리는 다시 웃을 수 있다.",
        )
    }

    private fun whiteCastleGreeting(id: PlaceId): String? = when (id) {
        PlaceId.HOME -> "왕실 숙소에 햇살이 든다. 해방된 성의 온기가 감싼다."
        PlaceId.SHOP -> "\"용사님! 해방된 시장에 어서 오세요.\""
        PlaceId.WEAPON_SHOP -> "\"해골 왕을 쓰러뜨린 분…! 병기를 빛내 드리겠습니다.\""
        PlaceId.HOSPITAL -> "\"저주의 상처가 아물어 갑니다. 감사합니다, 용사님.\""
        PlaceId.CHURCH -> "\"전설이 이뤄졌소. 빛이 다시 이 성을 감싸오.\""
        PlaceId.INN -> "\"해방 축제 방이 준비돼 있어요. 편히 쉬세요!\""
        PlaceId.PUB -> "축제의 홀에서 감사와 노래가 넘친다. 용사의 이름이 불린다."
        PlaceId.ARENA -> "\"기사 훈련장이 열렸소! 해방의 검을 기리자!\""
        PlaceId.BLACKSMITH -> "\"해방의 쇠를 두드리겠소. 고맙소, 용사여!\""
        PlaceId.MAGIC_SCHOOL -> "\"저주가 끊겼네. 당신이 전설을 이뤄 주었지.\""
        PlaceId.MERCENARY -> "\"해방된 기사단이 당신을 기다렸소!\""
        else -> "하얀 성… 저주가 풀린 뒤, 사람들의 감사가 거리에 흐른다."
    }

    private val whiteCastlePub = listOf(
        PubNpc(
            id = "owner", name = "보릭", role = "연회 주인", kind = NpcKind.OWNER,
            x = 805f, y = 390f, spriteKey = "merchant",
            lines = listOf(
                "용사여! 잔을 들자. 저주가 풀렸다!",
                "당신이 해골 왕을 쓰러뜨려 성을 되살렸소.",
                "전설이 이뤄졌네. 이 연회는 당신 몫이야.",
            )
        ),
        PubNpc(
            id = "traveler", name = "엘린", role = "시인", kind = NpcKind.TRAVELER,
            x = 335f, y = 245f, spriteKey = "rogue",
            lines = listOf(
                "회색 성이 하얗게… 노래로 남기겠어요.",
                "용사가 저주를 풀어 줬다. 고맙습니다!",
                "전설이 현실이 된 날을 목격하다니.",
            )
        ),
        PubNpc(
            id = "guild", name = "케인", role = "해방 기사", kind = NpcKind.GUILD_MEMBER,
            x = 565f, y = 345f, spriteKey = "warrior",
            lines = listOf(
                "당신 덕분에 우리가 사람으로 돌아왔소.",
                "해골 왕의 왕좌가 무너진 날… 잊지 않겠소.",
                "전설을 이룬 자와 어깨를 나란히 하다니 영광이오.",
            )
        ),
        PubNpc(
            id = "drunk", name = "토드", role = "축객", kind = NpcKind.DRUNK,
            x = 210f, y = 505f, spriteKey = "farmer",
            lines = listOf(
                "히끅… 포도주가… 다시 달다…!",
                "용사… 고맙다… 저주가… 사라졌어…!",
                "전설이… 히끅… 이뤄졌다고…!",
            )
        ),
    )
}
