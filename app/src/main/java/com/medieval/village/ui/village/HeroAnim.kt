package com.medieval.village.ui.village

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.model.HeroJob
import kotlin.math.floor

/**
 * 히어로 애니메이션 프레임 선택.
 * walk_side / walk_down / slash / bow / magic 시트에서 고른다.
 * specialSet 이 있으면 특별스킬 전용 시트(adv_smash 등)를 우선한다.
 */
fun CustomArt.heroAnimSprite(
    kind: HeroAnimKind,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    animFrame: Int,
    specialSet: String? = null,
    heroJob: HeroJob = HeroJob.WARRIOR,
): ImageBitmap? {
    val useWarriorAnims = heroJob == HeroJob.WARRIOR
    if (!specialSet.isNullOrBlank()) {
        if (useWarriorAnims) {
            heroAnimFrameOrNull(specialSet, animFrame)?.let { return it }
        } else {
            heroAnimFrameOrNull("${heroJob.id}_$specialSet", animFrame)?.let { return it }
        }
    }
    val attackKey = when (kind) {
        HeroAnimKind.SLASH -> "slash"
        HeroAnimKind.BOW -> "bow"
        HeroAnimKind.MAGIC -> "magic"
        else -> null
    }
    if (attackKey != null) {
        if (useWarriorAnims) {
            return heroAnimFrameOrNull(attackKey, animFrame)
                ?: heroSpriteOrNull("side", heroJob)
        }
        heroAnimFrameOrNull("${heroJob.id}_$attackKey", animFrame)?.let { return it }
        return heroSpriteOrNull("side", heroJob)
            ?: heroSpriteOrNull("front", heroJob)
    }

    val useWalk = kind == HeroAnimKind.WALK || (kind == HeroAnimKind.IDLE && walking)
    if (useWarriorAnims) {
        val walkKey = when (facing) {
            Facing.DOWN -> "walk_down"
            Facing.UP -> null // 후면 걷기 시트 없음 → 정적 back
            Facing.LEFT, Facing.RIGHT -> "walk_side"
        }
        if (walkKey != null) {
            val frame = if (useWalk) walkFrameIndex(walkPhase) else 0
            heroAnimFrameOrNull(walkKey, frame)?.let { return it }
        }
    } else {
        val walkKey = when (facing) {
            Facing.DOWN -> "${heroJob.id}_walk_down"
            Facing.UP -> null
            Facing.LEFT, Facing.RIGHT -> "${heroJob.id}_walk_side"
        }
        if (walkKey != null && useWalk) {
            heroAnimFrameOrNull(walkKey, walkFrameIndex(walkPhase))?.let { return it }
        }
    }
    val staticKey = when (facing) {
        Facing.UP -> "back"
        Facing.LEFT, Facing.RIGHT -> "side"
        Facing.DOWN -> "front"
    }
    return heroSpriteOrNull(staticKey, heroJob)
        ?: heroSpriteOrNull("front", heroJob)
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
    specialSet: String? = null,
    heroJob: HeroJob = HeroJob.WARRIOR,
) {
    val sprite = art.heroAnimSprite(
        kind = animKind,
        facing = facing,
        walking = walking,
        walkPhase = walkPhase,
        animFrame = animFrame,
        specialSet = specialSet,
        heroJob = heroJob,
    ) ?: return
    // 공격 시트는 오른쪽 기준 → LEFT 일 때 반전. UP/DOWN 공격도 측면 시트 사용.
    val attacking = animKind == HeroAnimKind.SLASH ||
        animKind == HeroAnimKind.BOW ||
        animKind == HeroAnimKind.MAGIC ||
        specialSet != null
    val mirror = when {
        attacking -> facing == Facing.LEFT || facing == Facing.UP
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
