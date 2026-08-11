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
)

/** 근접 초승달 참격 연출 */
data class MeleeSlashFx(
    val x: Float,
    val y: Float,
    val facing: Facing,
    var age: Float = 0f,
    val duration: Float = 0.28f,
) {
    val progress: Float get() = (age / duration).coerceIn(0f, 1f)
    val alive: Boolean get() = age < duration
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
