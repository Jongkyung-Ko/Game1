package com.medieval.village.ui.village

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import kotlin.math.floor

/**
 * 히어로 애니메이션 프레임 선택.
 * walk_side / walk_down / slash / bow / magic 시트에서 고른다.
 */
fun CustomArt.heroAnimSprite(
    kind: HeroAnimKind,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    animFrame: Int,
): ImageBitmap? {
    val attackKey = when (kind) {
        HeroAnimKind.SLASH -> "slash"
        HeroAnimKind.BOW -> "bow"
        HeroAnimKind.MAGIC -> "magic"
        else -> null
    }
    if (attackKey != null) {
        return heroAnimFrameOrNull(attackKey, animFrame)
            ?: heroSpriteOrNull("side")
    }

    val useWalk = kind == HeroAnimKind.WALK || (kind == HeroAnimKind.IDLE && walking)
    val walkKey = when (facing) {
        Facing.DOWN -> "walk_down"
        Facing.UP -> null // 후면 걷기 시트 없음 → 정적 back
        Facing.LEFT, Facing.RIGHT -> "walk_side"
    }
    if (walkKey != null) {
        val frame = if (useWalk) walkFrameIndex(walkPhase) else 0
        heroAnimFrameOrNull(walkKey, frame)?.let { return it }
    }
    val staticKey = when (facing) {
        Facing.UP -> "back"
        Facing.LEFT, Facing.RIGHT -> "side"
        Facing.DOWN -> "front"
    }
    return heroSpriteOrNull(staticKey)
        ?: heroSpriteOrNull("front")
        ?: charOrNull("warrior")
}

/** 선두·후열 공통 — walkPhase 로 4프레임 순환 */
private fun walkFrameIndex(walkPhase: Float): Int {
    val idx = floor(((walkPhase % 6.2831855f) / 6.2831855f) * 4f).toInt()
    return ((idx % 4) + 4) % 4
}

fun DrawScope.drawAnimatedHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    worldHeight: Float,
    animKind: HeroAnimKind = HeroAnimKind.IDLE,
    animFrame: Int = 0,
) {
    val sprite = art.heroAnimSprite(animKind, facing, walking, walkPhase, animFrame) ?: return
    // 공격 시트는 오른쪽 기준 → LEFT 일 때 반전. UP/DOWN 공격도 측면 시트 사용.
    val mirror = when (animKind) {
        HeroAnimKind.SLASH, HeroAnimKind.BOW, HeroAnimKind.MAGIC ->
            facing == Facing.LEFT || facing == Facing.UP
        else -> facing == Facing.LEFT
    }
    drawCustomSprite(
        image = sprite,
        cx = x,
        footY = y,
        worldHeight = worldHeight,
        mirrorX = mirror,
    )
}
