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
                "황금 수사슴에 잘 왔네! 따뜻한 자리는 항상 남아 있지.",
                "던전에 갈 생각이면 물약과 횃불부터 챙기게.",
                "여행은 배가 든든해야 하는 법이야."
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
                "북쪽 교회에서는 기도만 해도 마나를 회복할 수 있대요.",
                "이 마을은 건물을 누르면 길을 따라 자동으로 걸어가요.",
                "멀리서 왔는데, 이곳 벽난로가 제일 따뜻하네요."
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
                "Status에서 고용한 용병 중 두 명을 원정대로 선택할 수 있어.",
                "대장간 강화는 단계가 높을수록 실패하기 쉬우니 골드를 모아 둬.",
                "던전 기록은 다음 탐험 난이도를 결정하지. 준비를 단단히 해."
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
                "히끅... 우물 밑에 보물이 있다던데... 아마 꿈이었나?",
                "용병은 둘이면 충분해! 셋이면 술값이 너무 많이 나와!",
                "대련소의 산적 두목은 왼쪽 공격에 약하다더군... 히끅."
            )
        )
    )
}
