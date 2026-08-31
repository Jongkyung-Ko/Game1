package com.medieval.village.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.medieval.village.R

enum class Sfx {
    HIT,
    /** 화살 적중 */
    ARROW_HIT,
    /** 마법 적중 */
    MAGIC_HIT,
    /** 기본 마력탄 발사 (MP 없음) */
    MAGIC_SHOT,
    DOOR,
    CLICK,
    /** 레벨업 */
    LEVEL_UP,
    // —— 특별스킬 ——
    SKILL_SMASH,
    SKILL_SLASH,
    SKILL_CHARGE,
    SKILL_BOW,
    SKILL_FIRE,
    SKILL_ICE,
    SKILL_LIGHTNING,
    SKILL_HOLY,
    SKILL_CRIT,
    SKILL_SPIN,
    SKILL_BASH,
    SKILL_EXECUTE,
    SKILL_ORB,
    SKILL_SMOKE,
    SKILL_QUAKE,
    SKILL_FINISHER,
}

/**
 * CC0 무료 개발자용 MP3 리소스(res/raw)를 MediaPlayer / SoundPool로 재생한다.
 * 정착지·저주/해방 상태별 BGM과 발소리·전투 효과음을 담당한다.
 */
class GameAudioEngine(context: Context) {

    private val appContext = context.applicationContext
    private var musicPlayer: MediaPlayer? = null
    private var footstepPlayer: MediaPlayer? = null
    private var currentMood: MusicMood? = null
    private var released = false
    private var walking = false
    private var bgmMul = 1f
    private var sfxMul = 1f

    private val soundPool: SoundPool? = runCatching {
        SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }.getOrNull()

    private val sfxIds: Map<Sfx, Int> = soundPool?.let { pool ->
        val hit = pool.load(appContext, R.raw.sfx_hit, 1)
        mapOf(
            Sfx.HIT to hit,
            Sfx.ARROW_HIT to hit,
            Sfx.MAGIC_HIT to hit,
            Sfx.MAGIC_SHOT to pool.load(appContext, R.raw.sfx_magic_shot, 1),
            Sfx.DOOR to pool.load(appContext, R.raw.sfx_door, 1),
            Sfx.CLICK to pool.load(appContext, R.raw.sfx_click, 1),
            Sfx.LEVEL_UP to pool.load(appContext, R.raw.sfx_level_up, 1),
            Sfx.SKILL_SMASH to pool.load(appContext, R.raw.sfx_skill_smash, 1),
            Sfx.SKILL_SLASH to pool.load(appContext, R.raw.sfx_skill_slash, 1),
            Sfx.SKILL_CHARGE to pool.load(appContext, R.raw.sfx_skill_charge, 1),
            Sfx.SKILL_BOW to pool.load(appContext, R.raw.sfx_skill_bow, 1),
            Sfx.SKILL_FIRE to pool.load(appContext, R.raw.sfx_skill_fire, 1),
            Sfx.SKILL_ICE to pool.load(appContext, R.raw.sfx_skill_ice, 1),
            Sfx.SKILL_LIGHTNING to pool.load(appContext, R.raw.sfx_skill_lightning, 1),
            Sfx.SKILL_HOLY to pool.load(appContext, R.raw.sfx_skill_holy, 1),
            Sfx.SKILL_CRIT to pool.load(appContext, R.raw.sfx_skill_crit, 1),
            Sfx.SKILL_SPIN to pool.load(appContext, R.raw.sfx_skill_spin, 1),
            Sfx.SKILL_BASH to pool.load(appContext, R.raw.sfx_skill_bash, 1),
            Sfx.SKILL_EXECUTE to pool.load(appContext, R.raw.sfx_skill_execute, 1),
            Sfx.SKILL_ORB to pool.load(appContext, R.raw.sfx_skill_orb, 1),
            Sfx.SKILL_SMOKE to pool.load(appContext, R.raw.sfx_skill_smoke, 1),
            Sfx.SKILL_QUAKE to pool.load(appContext, R.raw.sfx_skill_quake, 1),
            Sfx.SKILL_FINISHER to pool.load(appContext, R.raw.sfx_skill_finisher, 1),
        )
    } ?: emptyMap()

    fun playMusic(mood: MusicMood) {
        if (released || mood == currentMood) return
        currentMood = mood
        android.util.Log.i("GameBgm", "play $mood")
        runCatching { musicPlayer?.release() }
        musicPlayer = null
        val resId = when (mood) {
            MusicMood.OAKHAVEN -> R.raw.bgm_village
            MusicMood.ASHBROOK -> R.raw.bgm_ashbrook
            MusicMood.GRAY_CURSED -> R.raw.bgm_gray_cursed
            MusicMood.GRAY_LIBERATED -> R.raw.bgm_gray_liberated
            MusicMood.IGLOO_CURSED -> R.raw.bgm_igloo_cursed
            MusicMood.IGLOO_LIBERATED -> R.raw.bgm_igloo_liberated
            MusicMood.SEASIDE_CURSED -> R.raw.bgm_seaside_cursed
            MusicMood.SEASIDE_LIBERATED -> R.raw.bgm_seaside_liberated
            MusicMood.WINTER_CURSED -> R.raw.bgm_winter_cursed
            MusicMood.WINTER_LIBERATED -> R.raw.bgm_winter_liberated
            MusicMood.COZY -> R.raw.bgm_cozy
            MusicMood.TENSE -> R.raw.bgm_dungeon
        }
        musicPlayer = runCatching {
            MediaPlayer.create(appContext, resId)?.apply {
                isLooping = true
                val v = moodVolume(mood)
                setVolume(v, v)
                start()
            }
        }.getOrNull()
    }

    fun setWalking(walking: Boolean) {
        if (released) return
        this.walking = walking
        if (!walking) {
            footstepPlayer?.pause()
            return
        }
        if (footstepPlayer == null) {
            footstepPlayer = runCatching {
                MediaPlayer.create(appContext, R.raw.sfx_footstep)?.apply {
                    isLooping = true
                    setVolume(0.55f * sfxMul, 0.55f * sfxMul)
                }
            }.getOrNull()
        }
        if (footstepPlayer?.isPlaying != true) {
            runCatching { footstepPlayer?.start() }
        }
    }

    fun playSfx(sfx: Sfx) {
        if (released) return
        val pool = soundPool ?: return
        val id = sfxIds[sfx] ?: return
        val (vol, rate) = when (sfx) {
            Sfx.ARROW_HIT -> 0.75f to 1.35f
            Sfx.MAGIC_HIT -> 0.8f to 0.72f
            Sfx.MAGIC_SHOT -> 0.78f to 1f
            Sfx.HIT -> 0.75f to 1f
            Sfx.LEVEL_UP -> 0.92f to 1f
            Sfx.SKILL_SMASH, Sfx.SKILL_CRIT, Sfx.SKILL_QUAKE, Sfx.SKILL_BASH -> 0.9f to 1f
            Sfx.SKILL_SLASH, Sfx.SKILL_SPIN, Sfx.SKILL_CHARGE -> 0.85f to 1f
            Sfx.SKILL_BOW -> 0.8f to 1f
            Sfx.SKILL_FIRE, Sfx.SKILL_ICE, Sfx.SKILL_LIGHTNING, Sfx.SKILL_ORB -> 0.85f to 1f
            Sfx.SKILL_HOLY, Sfx.SKILL_FINISHER -> 0.88f to 1f
            Sfx.SKILL_EXECUTE, Sfx.SKILL_SMOKE -> 0.82f to 1f
            else -> 0.7f to 1f
        }
        val v = (vol * sfxMul).coerceIn(0f, 1f)
        runCatching { pool.play(id, v, v, 1, 0, rate) }
    }

    fun setUserVolume(bgm: Float, sfx: Float) {
        bgmMul = bgm.coerceIn(0f, 1f)
        sfxMul = sfx.coerceIn(0f, 1f)
        applyMusicVolume()
        val step = 0.55f * sfxMul
        runCatching { footstepPlayer?.setVolume(step, step) }
    }

    private fun moodVolume(mood: MusicMood): Float {
        val base = when (mood) {
            MusicMood.TENSE -> 0.42f
            MusicMood.COZY -> 0.34f
            MusicMood.GRAY_CURSED, MusicMood.IGLOO_CURSED,
            MusicMood.SEASIDE_CURSED, MusicMood.WINTER_CURSED -> 0.38f
            MusicMood.GRAY_LIBERATED, MusicMood.IGLOO_LIBERATED,
            MusicMood.SEASIDE_LIBERATED, MusicMood.WINTER_LIBERATED -> 0.37f
            MusicMood.OAKHAVEN, MusicMood.ASHBROOK -> 0.36f
        }
        return (base * bgmMul).coerceIn(0f, 1f)
    }

    private fun applyMusicVolume() {
        val mood = currentMood ?: return
        val v = moodVolume(mood)
        runCatching { musicPlayer?.setVolume(v, v) }
    }

    fun pause() {
        runCatching { musicPlayer?.pause() }
        runCatching { footstepPlayer?.pause() }
    }

    fun resume() {
        if (released) return
        if (currentMood != null) runCatching { musicPlayer?.start() }
        if (walking) runCatching { footstepPlayer?.start() }
    }

    fun release() {
        released = true
        runCatching { musicPlayer?.release() }
        runCatching { footstepPlayer?.release() }
        musicPlayer = null
        footstepPlayer = null
        runCatching { soundPool?.release() }
    }
}
