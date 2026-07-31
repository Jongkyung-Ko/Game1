package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kenney Tiny Dungeon 크리처 타일로 새·개·닭 등을 움직인다.
 */
fun DrawScope.drawVillageLife(atlas: KenneyAtlas, animTime: Float) {
    drawBirds(atlas, animTime)
    drawDog(atlas, animTime)
    drawCat(atlas, animTime)
    drawChickens(atlas, animTime)
    drawButterflies(atlas, animTime)
}

private fun DrawScope.drawBirds(atlas: KenneyAtlas, t: Float) {
    val church = Village.of(PlaceId.CHURCH)
    for (i in 0..4) {
        val a = t * (0.7f + i * 0.08f) + i * 1.1f
        val r = 70f + i * 18f
        val x = church.cx + cos(a) * r
        val y = church.top - 30f + sin(a * 1.4f) * 18f - i * 8f
        val flap = sin(t * 16f + i) * 3f
        drawKenneySprite(
            atlas.dungeon,
            DungeonTiles.BAT,
            x,
            y,
            WORLD_TILE * 1.1f,
            bob = flap,
            mirrorX = cos(a) < 0f
        )
    }
    for (i in 0..2) {
        val progress = ((t * 0.12f + i * 0.33f) % 1f)
        val x = 80f + progress * (Village.W - 160f)
        val y = 420f + sin(progress * PI.toFloat() * 4f + i) * 40f + i * 55f
        drawKenneySprite(
            atlas.dungeon,
            DungeonTiles.BAT,
            x,
            y,
            WORLD_TILE * 1.0f,
            bob = sin(t * 18f + i) * 2.5f,
            mirrorX = false
        )
    }
}

private fun DrawScope.drawDog(atlas: KenneyAtlas, t: Float) {
    val a = t * 0.45f
    val x = Village.WELL_X + cos(a) * 95f
    val y = Village.WELL_Y + sin(a) * 55f + 10f
    val facingLeft = cos(a + PI.toFloat() / 2f) < 0f
    drawKenneySprite(
        atlas.dungeon,
        DungeonTiles.BLOB,
        x,
        y,
        WORLD_TILE * 1.4f,
        bob = sin(t * 8f) * 2f,
        mirrorX = facingLeft
    )
}

private fun DrawScope.drawCat(atlas: KenneyAtlas, t: Float) {
    val shop = Village.of(PlaceId.WEAPON_SHOP)
    val pace = sin(t * 0.35f)
    val x = shop.cx + pace * (shop.w * 0.28f)
    val y = shop.top + shop.h * 0.42f
    drawKenneySprite(
        atlas.dungeon,
        DungeonTiles.SPIDER,
        x,
        y,
        WORLD_TILE * 1.15f,
        bob = sin(t * 5f) * 1.5f,
        mirrorX = cos(t * 0.35f) < 0f
    )
}

private fun DrawScope.drawChickens(atlas: KenneyAtlas, t: Float) {
    val home = Village.of(PlaceId.HOME)
    val baseX = home.left - 24f
    val baseY = home.doorY + 4f
    for (i in 0..2) {
        val x = baseX + i * 28f + sin(t * 0.8f + i) * 12f
        val y = baseY + cos(t * 0.6f + i) * 6f
        drawKenneySprite(
            atlas.dungeon,
            DungeonTiles.SLIME,
            x,
            y,
            WORLD_TILE * 1.15f,
            bob = sin(t * 9f + i) * 2f,
            mirrorX = sin(t * 0.8f + i) < 0f
        )
    }
}

private fun DrawScope.drawButterflies(atlas: KenneyAtlas, t: Float) {
    val spots = listOf(352f to 640f, 640f to 860f, 300f to 1180f, 720f to 480f)
    spots.forEachIndexed { i, (sx, sy) ->
        val a = t * (1.4f + i * 0.2f) + i
        drawKenneySprite(
            atlas.dungeon,
            DungeonTiles.BAT,
            sx + cos(a) * 28f,
            sy + sin(a * 1.7f) * 18f,
            WORLD_TILE * 0.45f,
            bob = sin(t * 16f + i) * 2f,
            mirrorX = i % 2 == 0
        )
    }
}
