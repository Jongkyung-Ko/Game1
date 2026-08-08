package com.medieval.village.model

enum class InteriorNpcKind {
    KEEPER,   // 주인/점원
    HELPER,   // 조수
    VISITOR   // 손님/신자 등
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
    val lines: List<String>
) {
    val worldX: Float get() = fx * InteriorRoom.WORLD_W
    val worldY: Float get() = fy * InteriorRoom.WORLD_H
}

object InteriorNpcCatalog {

    fun forPlace(placeId: PlaceId): List<InteriorNpc> = all.filter { it.placeId == placeId }

    val all: List<InteriorNpc> = listOf(
        npc(PlaceId.HOME, "home_sister", "미라", "동생", InteriorNpcKind.HELPER, 0.72f, 0.86f,
            "오빠, 오늘은 조심해서 다녀와.", "창밖에서 또 새가 울어.", "좀비석 얘기는… 무서워."),
        npc(PlaceId.SHOP, "shop_keeper", "한나", "잡화 주인", InteriorNpcKind.KEEPER, 0.68f, 0.84f,
            "어서 와요! 횃불이랑 붕대 챙기셨나요?", "오늘 빵은 따끈해요.", "던전 가시면 물약은 필수예요."),
        npc(PlaceId.SHOP, "shop_boy", "펠", "점원", InteriorNpcKind.HELPER, 0.82f, 0.88f,
            "손님, 이쪽 선반 보세요!", "재고는 매일 아침 채워요.", "안녕하세요!"),
        npc(PlaceId.WEAPON_SHOP, "weapon_smith", "가렌", "무기상", InteriorNpcKind.KEEPER, 0.70f, 0.84f,
            "좋은 쇠를 찾는군.", "좀비 뼈도 가르려면 날이 살아야지.", "방패부터 챙기는 게 현명해."),
        npc(PlaceId.HOSPITAL, "doc", "엘라", "의사", InteriorNpcKind.KEEPER, 0.66f, 0.84f,
            "어디가 아파서 오셨습니까?", "물린 상처는 바로 소독해야 해요.", "해독 영양제도 준비해 두었어요."),
        npc(PlaceId.HOSPITAL, "nurse", "니나", "간호", InteriorNpcKind.HELPER, 0.80f, 0.88f,
            "침대는 이쪽이에요.", "깊게 숨 쉬어 보세요.", "회복을 빌어요."),
        npc(PlaceId.CHURCH, "priest", "마르코", "사제", InteriorNpcKind.KEEPER, 0.70f, 0.84f,
            "빛의 가호가 함께하기를.", "저주를 씻는 기도를 올리겠소.", "헌금은 마을을 지키는 불빛이오."),
        npc(PlaceId.CHURCH, "nun", "세라", "수녀", InteriorNpcKind.HELPER, 0.84f, 0.88f,
            "편히 기도하세요.", "마음이 무겁다면 말씀해 주세요.", "평안을 빕니다."),
        npc(PlaceId.INN, "innkeep", "롤프", "여관 주인", InteriorNpcKind.KEEPER, 0.68f, 0.84f,
            "방 하나 잡으시겠어요?", "문은 꼭 잠그세요.", "밤에 하수도 기척이 들린다오."),
        npc(PlaceId.INN, "maid", "루나", "하녀", InteriorNpcKind.HELPER, 0.82f, 0.88f,
            "이불은 따뜻하게 갈아두었어요.", "소문 들으러 오신 건가요?", "편히 쉬세요."),
        npc(PlaceId.ARENA, "coach", "브룬", "교관", InteriorNpcKind.KEEPER, 0.70f, 0.84f,
            "몸 좀 풀러 왔나?", "지상에서라도 칼날을 갈아야지.", "이기면 상금이다!"),
        npc(PlaceId.BLACKSMITH, "forge", "드루", "대장장이", InteriorNpcKind.KEEPER, 0.68f, 0.84f,
            "쇠는 두들길수록 강해지지.", "좀비 이빨에 안 깨지려면 더 달궈야 해.", "강화는 신중히."),
        npc(PlaceId.MAGIC_SCHOOL, "mage", "이리스", "마법사", InteriorNpcKind.KEEPER, 0.70f, 0.84f,
            "해독 연구에 오신 건가.", "마법은 반복일세.", "좀비석 기운은 함부로 만지지 말게."),
        npc(PlaceId.MAGIC_SCHOOL, "apprentice", "얀", "견습", InteriorNpcKind.HELPER, 0.84f, 0.88f,
            "고서가 또 날아갔어요…", "화염구 수업은 오후예요.", "안녕하세요!"),
        npc(PlaceId.MERCENARY, "recruiter", "오스카", "용병대장", InteriorNpcKind.KEEPER, 0.68f, 0.84f,
            "돈만 주면 붙여주지.", "좀비 둥지 안내라면 목숨값은 별도야.", "원정대는 둘이면 충분해."),
        npc(PlaceId.MERCENARY, "rookie", "팀", "신입 용병", InteriorNpcKind.VISITOR, 0.84f, 0.88f,
            "저도 곧 나갈 거예요!", "칼 좀 빌려주시겠어요…?", "파이팅!")
    )

    private fun npc(
        placeId: PlaceId,
        id: String,
        name: String,
        role: String,
        kind: InteriorNpcKind,
        fx: Float,
        fy: Float,
        vararg lines: String
    ) = InteriorNpc(id, placeId, name, role, kind, fx, fy, lines.toList())
}
