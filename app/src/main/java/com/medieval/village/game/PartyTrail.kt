package com.medieval.village.game

import kotlin.math.abs
import kotlin.math.hypot

/** 선두가 지나간 길을 기록해 후열이 부드럽게 따라오게 한다. */
class PartyTrail(
    private val sampleDist: Float = 6f,
    private val maxPoints: Int = 128,
) {
    data class Point(val x: Float, val y: Float, val facing: Facing)

    private val points = ArrayDeque<Point>()

    fun clear() {
        points.clear()
    }

    fun reset(x: Float, y: Float, facing: Facing) {
        points.clear()
        // 후열이 바로 선두 뒤로 서도록 짧은 직선 궤적을 미리 심는다
        val (bx, by) = PartyFormation.behindOffset(facing, 1)
        val seed = 4
        for (i in seed downTo 1) {
            val t = i.toFloat() / seed
            points.addLast(
                Point(
                    x = x + bx * t * 2.2f,
                    y = y + by * t * 2.2f,
                    facing = facing,
                )
            )
        }
        points.addLast(Point(x, y, facing))
    }

    /** 선두가 이동했을 때 호출 */
    fun record(x: Float, y: Float, facing: Facing) {
        val last = points.lastOrNull()
        if (last == null) {
            points.addLast(Point(x, y, facing))
            return
        }
        if (hypot(x - last.x, y - last.y) < sampleDist) {
            // 같은 자리에서도 방향만 갱신
            if (last.facing != facing) {
                points.removeLast()
                points.addLast(Point(x, y, facing))
            }
            return
        }
        points.addLast(Point(x, y, facing))
        while (points.size > maxPoints) points.removeFirst()
    }

    /**
     * 선두로부터 [distance] 만큼 뒤를 따라간 위치.
     * 트레일이 짧으면 마지막 점 + facing 오프셋으로 폴백.
     */
    fun positionBehind(
        distance: Float,
        leadX: Float,
        leadY: Float,
        leadFacing: Facing,
    ): Point {
        if (distance <= 0.5f) {
            return Point(leadX, leadY, leadFacing)
        }
        if (points.isEmpty()) {
            val slots = (distance / PartyFormation.SPACING).coerceAtLeast(1f)
            val (ox, oy) = PartyFormation.behindOffset(leadFacing, 1)
            return Point(
                x = leadX + ox * slots,
                y = leadY + oy * slots,
                facing = leadFacing,
            )
        }
        // 선두 현재 위치를 끝점으로 두고 역추적
        var prevX = leadX
        var prevY = leadY
        var prevFacing = leadFacing
        var remaining = distance

        for (i in points.lastIndex downTo 0) {
            val p = points[i]
            val seg = hypot(prevX - p.x, prevY - p.y)
            if (seg < 0.01f) {
                prevFacing = p.facing
                continue
            }
            if (remaining <= seg) {
                val t = remaining / seg
                // 경로를 따라 선두 쪽을 바라보게
                val face = facingAlong(prevX - p.x, prevY - p.y, prevFacing)
                return Point(
                    x = prevX + (p.x - prevX) * t,
                    y = prevY + (p.y - prevY) * t,
                    facing = face,
                )
            }
            remaining -= seg
            prevFacing = facingAlong(prevX - p.x, prevY - p.y, p.facing)
            prevX = p.x
            prevY = p.y
        }
        val (ox, oy) = PartyFormation.behindOffset(prevFacing, 1)
        val slots = (distance / PartyFormation.SPACING).coerceAtLeast(1f)
        return Point(
            x = prevX + ox * slots,
            y = prevY + oy * slots,
            facing = prevFacing,
        )
    }

    private fun facingAlong(dx: Float, dy: Float, fallback: Facing): Facing {
        if (abs(dx) < 0.01f && abs(dy) < 0.01f) return fallback
        return if (abs(dx) > abs(dy)) {
            if (dx > 0f) Facing.RIGHT else Facing.LEFT
        } else {
            if (dy > 0f) Facing.DOWN else Facing.UP
        }
    }
}

/** 그리기용 파티 슬롯 */
data class PartyDrawSlot(
    val mercenary: com.medieval.village.model.Mercenary?,
    val x: Float,
    val y: Float,
    val facing: Facing,
    val isFront: Boolean,
)
