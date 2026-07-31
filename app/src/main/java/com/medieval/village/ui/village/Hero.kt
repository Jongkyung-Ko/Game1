package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing

/** 커스텀 주인공 — 걷기 프레임 없이 방향별 정지 이미지만 사용. */
fun DrawScope.drawHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    // walking/animTime 무시: 요청대로 걷기 프레임·bob 연출 제거
    drawCustomHero(art, x, y, facing, worldHeight = 78f)
}

/** Kenney 폴백 (용병 등과 동일 스케일 비교용) */
fun DrawScope.drawHeroKenney(
    atlas: KenneyAtlas,
    x: Float,
    y: Float,
    facing: Facing,
) {
    drawKenneySpriteAsset(
        image = atlas.sprite("hero"),
        cx = x,
        footY = y,
        worldHeight = WORLD_TILE * 2.6f,
        bob = 0f,
        mirrorX = facing == Facing.LEFT,
    )
}
