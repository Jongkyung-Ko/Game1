package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawVillageLife(atlas: KenneyAtlas, animTime: Float) {
    // 새: 작은 크리처를 하늘에서 선회
    val church = Village.of(PlaceId.CHURCH)
    for (i in 0..3) {
        val a = animTime * (0.8f + i * 0.1f) + i
        val r = 80f + i * 22f
        drawKenneySpriteAsset(
            image = atlas.sprite("critter_c"),
            cx = church.cx + cos(a) * r,
            footY = church.top - 20f + sin(a * 1.5f) * 16f - i * 10f,
            worldHeight = WORLD_TILE * 0.85f,
            bob = sin(animTime * 16f + i) * 3f,
            mirrorX = cos(a) < 0f,
        )
    }
    // 개
    val dogA = animTime * 0.45f
    drawKenneySpriteAsset(
        image = atlas.sprite("critter_a"),
        cx = Village.WELL_X + cos(dogA) * 95f,
        footY = Village.WELL_Y + sin(dogA) * 55f + 10f,
        worldHeight = WORLD_TILE * 1.0f,
        bob = sin(animTime * 8f) * 2.5f,
        mirrorX = cos(dogA + PI.toFloat() / 2f) < 0f,
    )
    // 지붕 위 고양이
    val shop = Village.of(PlaceId.WEAPON_SHOP)
    drawKenneySpriteAsset(
        image = atlas.sprite("critter_b"),
        cx = shop.cx + sin(animTime * 0.35f) * shop.w * 0.25f,
        footY = shop.bottom - WORLD_TILE * 1.7f,
        worldHeight = WORLD_TILE * 0.9f,
        bob = sin(animTime * 5f) * 1.5f,
        mirrorX = cos(animTime * 0.35f) < 0f,
    )
    // 집 앞 작은 동물
    val home = Village.of(PlaceId.HOME)
    for (i in 0..2) {
        drawKenneySpriteAsset(
            image = atlas.sprite("critter_c"),
            cx = home.left - 20f + i * 30f + sin(animTime * 0.9f + i) * 12f,
            footY = home.doorY + 6f + cos(animTime * 0.7f + i) * 5f,
            worldHeight = WORLD_TILE * 0.85f,
            bob = sin(animTime * 9f + i) * 2f,
            mirrorX = sin(animTime * 0.9f + i) < 0f,
        )
    }
}
