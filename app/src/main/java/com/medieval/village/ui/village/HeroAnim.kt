package com.medieval.village.ui.village

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.model.HeroJob
import kotlin.math.floor

/**
 * 히어로 애니메이션 프레임 선택.
 * 직업·전직 랭크별 walk_side / walk_down / walk_up / slash / bow / magic 시트에서 고른다.
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
    heroRank: Int = 0,
): ImageBitmap? {
    if (!specialSet.isNullOrBlank()) {
        firstHeroAnim(heroJob, heroRank, specialSet, animFrame)?.let { return it }
    }
    val attackKey = when (kind) {
        HeroAnimKind.SLASH -> "slash"
        HeroAnimKind.BOW -> "bow"
        HeroAnimKind.MAGIC -> "magic"
        else -> null
    }
    if (attackKey != null) {
        firstHeroAnim(heroJob, heroRank, attackKey, animFrame)?.let { return it }
        val fallback = jobDefaultAttack(heroJob)
        if (fallback != attackKey) {
            firstHeroAnim(heroJob, heroRank, fallback, animFrame)?.let { return it }
        }
        return heroSpriteOrNull("side", heroJob, heroRank)
            ?: heroSpriteOrNull("front", heroJob, heroRank)
    }

    val useWalk = kind == HeroAnimKind.WALK || (kind == HeroAnimKind.IDLE && walking)
    val walkSuffix = when (facing) {
        Facing.DOWN -> "walk_down"
        Facing.UP -> "walk_up"
        Facing.LEFT, Facing.RIGHT -> "walk_side"
    }
    val frame = if (useWalk) walkFrameIndex(walkPhase) else 0
    firstHeroAnim(heroJob, heroRank, walkSuffix, frame)?.let { return it }
    val staticKey = when (facing) {
        Facing.UP -> "back"
        Facing.LEFT, Facing.RIGHT -> "side"
        Facing.DOWN -> "front"
    }
    return heroSpriteOrNull(staticKey, heroJob, heroRank)
        ?: heroSpriteOrNull("front", heroJob, heroRank)
        ?: charOrNull("warrior")
}

private fun jobDefaultAttack(job: HeroJob): String = when (job) {
    HeroJob.MAGE -> "magic"
    HeroJob.ARCHER -> "bow"
    else -> "slash"
}

/** 전직 랭크 시트 → 직업 기본 시트 → (용사 0랭크) 접두사 없는 시트 */
private fun CustomArt.firstHeroAnim(
    job: HeroJob,
    rank: Int,
    suffix: String,
    frame: Int,
): ImageBitmap? {
    for (key in heroAnimKeys(job, rank, suffix)) {
        heroAnimFrameOrNull(key, frame)?.let { return it }
    }
    return null
}

private fun heroAnimKeys(job: HeroJob, rank: Int, suffix: String): List<String> {
    val keys = ArrayList<String>(4)
    val r = rank.coerceAtLeast(0)
    if (r > 0) keys += "${job.id}_r${r}_$suffix"
    if (job == HeroJob.WARRIOR && r <= 0) {
        keys += suffix
    } else {
        keys += "${job.id}_$suffix"
        if (job == HeroJob.WARRIOR) keys += suffix
    }
    return keys
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
    heroRank: Int = 0,
) {
    val sprite = art.heroAnimSprite(
        kind = animKind,
        facing = facing,
        walking = walking,
        walkPhase = walkPhase,
        animFrame = animFrame,
        specialSet = specialSet,
        heroJob = heroJob,
        heroRank = heroRank,
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
