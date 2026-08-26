package com.medieval.village.game

import com.medieval.village.model.WeaponStyle

/** 주인공 스프라이트 시트 재생 종류 */
enum class HeroAnimKind {
    IDLE,
    WALK,
    SLASH,
    BOW,
    MAGIC,
}

/** 던전/탐험에서 발사되는 화살·마법 탄환 */
data class DungeonProjectile(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val style: WeaponStyle,
    val damage: Int,
    var life: Float,
    val radius: Float = 18f,
    /** 커스텀 탄환 스프라이트 세트 (예: adv_fx_firebolt) */
    val fxSpriteKey: String? = null,
    /** 명중 시 추가 버스트 FX (예: adv_fx_fireburst) */
    val impactSpriteKey: String? = null,
    /** 몬스터가 쏜 탄환 — 주인공을 노린다. */
    val hostile: Boolean = false,
)

/** 근접 초승달 참격 연출 */
data class MeleeSlashFx(
    val x: Float,
    val y: Float,
    val facing: Facing,
    var age: Float = 0f,
    val duration: Float = 0.34f,
    /** 1 = 일반, 특별스킬은 더 크게 */
    val power: Float = 1f,
) {
    val progress: Float get() = (age / duration).coerceIn(0f, 1f)
    val alive: Boolean get() = age < duration
}

/** 특별스킬 스프라이트 이펙트 (강타 충격파·필살 광선 등) */
data class SpecialSkillFx(
    val x: Float,
    val y: Float,
    val facing: Facing,
    val spriteKey: String,
    var age: Float = 0f,
    val duration: Float = 0.55f,
    val scale: Float = 1.45f,
) {
    val progress: Float get() = (age / duration).coerceIn(0f, 1f)
    val alive: Boolean get() = age < duration
    val frame: Int get() = (progress * 4f).toInt().coerceIn(0, 3)
}

fun Facing.dirX(): Float = when (this) {
    Facing.RIGHT -> 1f
    Facing.LEFT -> -1f
    else -> 0f
}

fun Facing.dirY(): Float = when (this) {
    Facing.DOWN -> 1f
    Facing.UP -> -1f
    else -> 0f
}
