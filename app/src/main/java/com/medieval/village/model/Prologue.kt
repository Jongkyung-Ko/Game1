package com.medieval.village.model

data class PrologueSlide(
    val asset: String,
    val title: String,
    val body: String,
) {
    val spoken: String get() = "$title. $body"
}

object Prologue {
    val slides: List<PrologueSlide> = listOf(
        PrologueSlide(
            asset = "ui/intro_01.jpg",
            title = "평화로운 마을",
            body = "사람들은 포도밭과 장터에서 하루를 보내며 웃고 살았다. 언덕 위 성과 오두막이 햇살 아래 고요히 이어져 있었다.",
        ),
        PrologueSlide(
            asset = "ui/intro_02.jpg",
            title = "금을 찾아",
            body = "땅속 깊은 곳에 금이 묻혀 있다는 소문이 퍼지자, 마을 사람들은 곡괭이와 횃불을 들고 땅을 깊게 파기 시작했다.",
        ),
        PrologueSlide(
            asset = "ui/intro_03.jpg",
            title = "지하의 저주",
            body = "어느 순간, 지하 던전에 머물던 이들이 좀비가 되어 일어났다. 금을 쫓던 갱도는 검붉은 저주의 길이 되었다.",
        ),
        PrologueSlide(
            asset = "ui/intro_04.jpg",
            title = "덮친 재앙",
            body = "그 재앙은 한 마을에서 끝나지 않았다. 숲과 사막, 얼음 마을까지 천재지변이 덮치고 사람들은 달아났다.",
        ),
        PrologueSlide(
            asset = "ui/intro_05.jpg",
            title = "모험의 시작",
            body = "주인공 일행은 이 일을 끝내기 위해 횃불을 들고 던전으로 모험을 떠난다.",
        ),
    )
}
