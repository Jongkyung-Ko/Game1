package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing

/** 커스텀 전사 스프라이트 — 걷기 프레임 없이 정지 이미지만 사용. */
fun DrawScope.drawHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    @Suppress("UNUSED_PARAMETER") walking: Boolean,
    @Suppress("UNUSED_PARAMETER") animTime: Float
) {
    drawCustomHero(art, x, y, facing, worldHeight = 78f)
}
