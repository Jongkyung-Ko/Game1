package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.game.PartyFormation
import com.medieval.village.model.Mercenary

/**
 * 마을·실내: 주인공이 항상 선두, 용병은 뒤에 일렬로 따라온다.
 * 뒤쪽부터 그려 앞사람이 위에 보이게 한다.
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
    mercScale: Float = 0.95f,
) {
    for (i in mercs.lastIndex downTo 0) {
        val (ox, oy) = PartyFormation.behindOffset(facing, i + 1)
        drawMercenary(
            mercenary = mercs[i],
            x = heroX + ox,
            y = heroY + oy,
            facing = facing,
            walking = walking,
            phase = walkPhase + (i + 1) * 0.55f,
            scale = mercScale,
            art = art,
            animKind = if (walking) HeroAnimKind.WALK else HeroAnimKind.IDLE,
            animFrame = 0,
        )
    }
    drawHero(
        heroX,
        heroY,
        facing,
        walking,
        walkPhase,
        scale = heroScale,
        art = art,
        animKind = if (walking) HeroAnimKind.WALK else HeroAnimKind.IDLE,
        animFrame = 0,
    )
}

/**
 * 던전·야외: [frontIndex] 가 선두(leadX/Y). 나머지는 뒤로 일렬.
 * 선두만 공격/걷기 프레임을 받고, 후열은 이동 시 걷기만.
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
    scale: Float = 0.78f,
) {
    val line = PartyFormation.battleLine(frontIndex, party)
    for (i in line.lastIndex downTo 0) {
        val (ox, oy) = PartyFormation.behindOffset(facing, i)
        val x = leadX + ox
        val y = leadY + oy
        val isFront = i == 0
        val animKind = when {
            isFront -> frontAnimKind
            walking -> HeroAnimKind.WALK
            else -> HeroAnimKind.IDLE
        }
        val animFrame = if (isFront) frontAnimFrame else 0
        val actor = line[i]
        if (actor == null) {
            drawHero(
                x,
                y,
                facing,
                walking = walking || isFront && frontAnimKind == HeroAnimKind.WALK,
                phase = walkPhase + i * 0.4f,
                scale = scale,
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
                walking = walking || (isFront && frontAnimKind == HeroAnimKind.WALK),
                phase = walkPhase + i * 0.4f,
                scale = scale * 0.92f,
                art = art,
                animKind = animKind,
                animFrame = animFrame,
            )
        }
    }
}

/** 선두 캐릭터의 참격 시트 키 (마법사는 cast) */
fun frontSlashAnimSet(frontMerc: Mercenary?): String = when {
    frontMerc == null -> "slash"
    frontMerc.spriteKey == "mage" -> "mage_cast"
    else -> "${frontMerc.spriteKey}_slash"
}
