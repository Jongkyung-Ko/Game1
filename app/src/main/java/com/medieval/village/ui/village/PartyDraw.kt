package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.game.PartyDrawSlot
import com.medieval.village.game.PartyFormation
import com.medieval.village.model.HeroJob
import com.medieval.village.model.Mercenary

/** 선두 대비 후열 크기 비율 */
const val PARTY_REAR_SCALE_FACTOR = 0.70f

/**
 * 궤적 추종 슬롯으로 파티를 그린다.
 * 선두(주인공·용병 공통)는 동일 크기, 후열은 선두의 70%.
 */
fun DrawScope.drawPartySlots(
    slots: List<PartyDrawSlot>,
    walking: Boolean,
    walkPhase: Float,
    frontAnimKind: HeroAnimKind,
    frontAnimFrame: Int,
    art: CustomArt?,
    scale: Float = 0.88f,
    rearScaleFactor: Float = PARTY_REAR_SCALE_FACTOR,
    specialAnimSet: String? = null,
    heroJob: HeroJob = HeroJob.WARRIOR,
) {
    // 뒤쪽부터 그려 선두가 위에
    for (i in slots.lastIndex downTo 0) {
        val slot = slots[i]
        val actorScale = if (slot.isFront) scale else scale * rearScaleFactor
        val phase = walkPhase + i * 0.55f
        val attacking = slot.isFront && (
            frontAnimKind == HeroAnimKind.SLASH ||
                frontAnimKind == HeroAnimKind.BOW ||
                frontAnimKind == HeroAnimKind.MAGIC ||
                specialAnimSet != null
            )
        val animKind = when {
            attacking -> frontAnimKind
            walking || (slot.isFront && frontAnimKind == HeroAnimKind.WALK) -> HeroAnimKind.WALK
            else -> HeroAnimKind.IDLE
        }
        val animFrame = if (attacking) frontAnimFrame else 0
        val actorWalking = walking || (slot.isFront && frontAnimKind == HeroAnimKind.WALK)
        val merc = slot.mercenary
        if (merc == null) {
            drawHero(
                slot.x,
                slot.y,
                slot.facing,
                walking = actorWalking,
                phase = phase,
                scale = actorScale,
                art = art,
                animKind = animKind,
                animFrame = animFrame,
                specialSet = if (slot.isFront) specialAnimSet else null,
                heroJob = heroJob,
            )
        } else {
            drawMercenary(
                mercenary = merc,
                x = slot.x,
                y = slot.y,
                facing = slot.facing,
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

/** 하위 호환: 궤적 없이 오프셋 배치 (폴백) */
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
    scale: Float = 0.88f,
    rearScaleFactor: Float = PARTY_REAR_SCALE_FACTOR,
    heroJob: HeroJob = HeroJob.WARRIOR,
) {
    val line = PartyFormation.battleLine(frontIndex, party)
    val slots = line.mapIndexed { i, actor ->
        val (ox, oy) = PartyFormation.behindOffset(facing, i)
        PartyDrawSlot(actor, leadX + ox, leadY + oy, facing, isFront = i == 0)
    }
    drawPartySlots(
        slots = slots,
        walking = walking,
        walkPhase = walkPhase,
        frontAnimKind = frontAnimKind,
        frontAnimFrame = frontAnimFrame,
        art = art,
        scale = scale,
        rearScaleFactor = rearScaleFactor,
        heroJob = heroJob,
    )
}

fun DrawScope.drawVillageFollowParty(
    heroX: Float,
    heroY: Float,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    mercs: List<Mercenary>,
    art: CustomArt?,
    heroScale: Float = 1f,
    /** @deprecated 후열은 항상 선두의 [PARTY_REAR_SCALE_FACTOR] */
    mercScale: Float = heroScale * PARTY_REAR_SCALE_FACTOR,
    frontIndex: Int = 0,
    frontAnimKind: HeroAnimKind = if (walking) HeroAnimKind.WALK else HeroAnimKind.IDLE,
    frontAnimFrame: Int = 0,
    slots: List<PartyDrawSlot>? = null,
    heroJob: HeroJob = HeroJob.WARRIOR,
) {
    if (slots != null) {
        drawPartySlots(
            slots = slots,
            walking = walking,
            walkPhase = walkPhase,
            frontAnimKind = frontAnimKind,
            frontAnimFrame = frontAnimFrame,
            art = art,
            scale = heroScale,
            rearScaleFactor = PARTY_REAR_SCALE_FACTOR,
            heroJob = heroJob,
        )
        return
    }
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
        rearScaleFactor = PARTY_REAR_SCALE_FACTOR,
        heroJob = heroJob,
    )
}

fun frontSlashAnimSet(frontMerc: Mercenary?): String = when {
    frontMerc == null -> "slash"
    frontMerc.spriteKey == "mage" -> "mage_cast"
    else -> "${frontMerc.spriteKey}_slash"
}
