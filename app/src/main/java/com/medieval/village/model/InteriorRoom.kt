package com.medieval.village.model

/** 선술집과 같은 스케일의 걸어다니는 실내 좌표계. */
object InteriorRoom {
    const val WORLD_W = 1000f
    const val WORLD_H = 700f

    /** 입구 쪽 스폰 (문 앞) */
    const val SPAWN_X = 220f
    const val SPAWN_Y = 580f

    fun clampX(x: Float): Float = x.coerceIn(90f, WORLD_W - 90f)
    fun clampY(y: Float): Float = y.coerceIn(200f, WORLD_H - 45f)
}
