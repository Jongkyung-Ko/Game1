package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.game.PartyFormation
import com.medieval.village.model.Mercenary

/**
 * 마을·실내·던전 공통: [frontIndex] 선두가 lead 위치, 나머지는 뒤 일렬.
 * 선두는 크게, 후열(주인공 포함)은 작게 그린다.
 */
fun DrawScope.drawBattleLineParty(
    leadX: Float,
    leadY: Float,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    frontIndex: Int,
    party: List<Mercenary>,
    frontAnimKind: HeroAnimKind,
    frontAnimFrame: Int,
    art: CustomArt?,
    /** 선두 크기 */
    scale: Float = 0.78f,
    /** 후열 = scale * rearScaleFactor */
    rearScaleFactor: Float = 0.82f,
) {
    val line = PartyFormation.battleLine(frontIndex, party)
    for (i in line.lastIndex downTo 0) {
        val (ox, oy) = PartyFormation.behindOffset(facing, i)
        val x = leadX + ox
        val y = leadY + oy
        val isFront = i == 0
        val actorScale = if (isFront) scale else scale * rearScaleFactor
        val phase = walkPhase + i * 0.55f
        val attacking = isFront && (
            frontAnimKind == HeroAnimKind.SLASH ||
                frontAnimKind == HeroAnimKind.BOW ||
                frontAnimKind == HeroAnimKind.MAGIC
            )
        val animKind = when {
            attacking -> frontAnimKind
            walking || (isFront && frontAnimKind == HeroAnimKind.WALK) -> HeroAnimKind.WALK
            else -> HeroAnimKind.IDLE
        }
        val animFrame = if (attacking) frontAnimFrame else 0
        val actorWalking = walking || (isFront && frontAnimKind == HeroAnimKind.WALK)
        val actor = line[i]
        if (actor == null) {
            drawHero(
                x,
                y,
                facing,
                walking = actorWalking,
                phase = phase,
                scale = actorScale,
                art = art,
                animKind = animKind,
                animFrame = animFrame,
            )
        } else {
            drawMercenary(
                mercenary = actor,
                x = x,
                y = y,
                facing = facing,
                walking = actorWalking,
                phase = phase,
                scale = actorScale,
                art = art,
                animKind = animKind,
                animFrame = animFrame,
            )
        }
    }
}

/**
 * 마을·실내용 래퍼 — 선두 교대를 반영한 일렬 종대.
 */
fun DrawScope.drawVillageFollowParty(
    heroX: Float,
    heroY: Float,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    mercs: List<Mercenary>,
    art: CustomArt?,
    heroScale: Float = 1f,
    mercScale: Float = 0.82f,
    frontIndex: Int = 0,
    frontAnimKind: HeroAnimKind = if (walking) HeroAnimKind.WALK else HeroAnimKind.IDLE,
    frontAnimFrame: Int = 0,
) {
    drawBattleLineParty(
        leadX = heroX,
        leadY = heroY,
        facing = facing,
        walking = walking,
        walkPhase = walkPhase,
        frontIndex = frontIndex,
        party = mercs,
        frontAnimKind = frontAnimKind,
        frontAnimFrame = frontAnimFrame,
        art = art,
        scale = heroScale,
        rearScaleFactor = if (heroScale <= 0f) 0.82f else mercScale / heroScale,
    )
}

/** 선두 캐릭터의 참격 시트 키 (마법사는 cast) */
fun frontSlashAnimSet(frontMerc: Mercenary?): String = when {
    frontMerc == null -> "slash"
    frontMerc.spriteKey == "mage" -> "mage_cast"
    else -> "${frontMerc.spriteKey}_slash"
}
