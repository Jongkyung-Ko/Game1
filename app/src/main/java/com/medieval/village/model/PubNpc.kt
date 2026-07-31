package com.medieval.village.model

enum class NpcKind {
    OWNER,
    TRAVELER,
    GUILD_MEMBER,
    DRUNK
}

data class PubNpc(
    val id: String,
    val name: String,
    val role: String,
    val kind: NpcKind,
    val x: Float,
    val y: Float,
    val lines: List<String>
)

object PubNpcCatalog {
    const val WORLD_W = 1000f
    const val WORLD_H = 700f

    val all = listOf(
        PubNpc(
            id = "owner",
            name = "보릭",
            role = "주인장",
            kind = NpcKind.OWNER,
            x = 805f,
            y = 390f,
            lines = listOf(
                "신성한 잔에 잘 왔네. 예전엔 포도주만으로도 마을이 웃었지…",
                "지하에서 캐낸 좀비석 얘기는 손님들 사이에서 끊이질 않아.",
                "던전에 갈 거면 횃불이랑 물약부터 챙기게. 저 아래는 더 이상 보관소가 아니야."
            )
        ),
        PubNpc(
            id = "traveler",
            name = "엘린",
            role = "여행객",
            kind = NpcKind.TRAVELER,
            x = 335f,
            y = 245f,
            lines = listOf(
                "풍요의 마을이라니… 표지판과 달리 공기가 무겁네요.",
                "영주가 병을 고치겠다고 좀비석을 만졌다는 소문을 들으러 왔어요.",
                "하수도 쪽에서 기척이 올라온대요. 밤에 문단속은 필수래요."
            )
        ),
        PubNpc(
            id = "guild",
            name = "케인",
            role = "길드 멤버",
            kind = NpcKind.GUILD_MEMBER,
            x = 565f,
            y = 345f,
            lines = listOf(
                "좀비 둥지는 라그나로크 던전처럼 직접 걸어 다니며 싸워야 해.",
                "오염된 사람들은 죽지도 못하고 뇌가 썩어, 생살 허기만 남았지.",
                "Status에서 용병 둘을 원정대로 뽑아. 심층일수록 혼자선 버겁다."
            )
        ),
        PubNpc(
            id = "drunk",
            name = "토드",
            role = "취객",
            kind = NpcKind.DRUNK,
            x = 210f,
            y = 505f,
            lines = listOf(
                "히끅… 신성한 포도주가… 검붉은 돌에 먹혀버렸어…",
                "연금술사 놈들… 목숨 늘리겠다고 하다가 마을을 좀비 우리로 만들었지!",
                "최하층에 좀비석이 아직 있다던데… 만지면… 히끅… 끝장이야…"
            )
        )
    )
}
