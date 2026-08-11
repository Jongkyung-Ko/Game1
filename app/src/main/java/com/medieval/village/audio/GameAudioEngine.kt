package com.medieval.village.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.medieval.village.R

enum class MusicMood {
    VILLAGE,
    COZY,
    TENSE
}

enum class Sfx {
    HIT,
    /** 화살 적중 */
    ARROW_HIT,
    /** 마법 적중 */
    MAGIC_HIT,
    DOOR,
    CLICK
}

/**
 * CC0 무료 개발자용 MP3 리소스(res/raw)를 MediaPlayer / SoundPool로 재생한다.
 * 마을·실내·던전 BGM과 발소리·전투 효과음을 담당한다.
 */
class GameAudioEngine(context: Context) {

    private val appContext = context.applicationContext
    private var musicPlayer: MediaPlayer? = null
    private var footstepPlayer: MediaPlayer? = null
    private var currentMood: MusicMood? = null
    private var released = false
    private var walking = false

    private val soundPool: SoundPool? = runCatching {
        SoundPool.Builder()
            .setMaxStreams(4)
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
            // 동일 히트음, 재생 속도로 화살/마법 구분
            Sfx.ARROW_HIT to hit,
            Sfx.MAGIC_HIT to hit,
            Sfx.DOOR to pool.load(appContext, R.raw.sfx_door, 1),
            Sfx.CLICK to pool.load(appContext, R.raw.sfx_click, 1)
        )
    } ?: emptyMap()

    fun playMusic(mood: MusicMood) {
        if (released || mood == currentMood) return
        currentMood = mood
        runCatching { musicPlayer?.release() }
        musicPlayer = null
        val resId = when (mood) {
            MusicMood.VILLAGE -> R.raw.bgm_village
            MusicMood.COZY -> R.raw.bgm_cozy
            MusicMood.TENSE -> R.raw.bgm_dungeon
        }
        musicPlayer = runCatching {
            MediaPlayer.create(appContext, resId)?.apply {
                isLooping = true
                setVolume(
                    when (mood) {
                        MusicMood.TENSE -> 0.42f
                        MusicMood.COZY -> 0.34f
                        MusicMood.VILLAGE -> 0.36f
                    },
                    when (mood) {
                        MusicMood.TENSE -> 0.42f
                        MusicMood.COZY -> 0.34f
                        MusicMood.VILLAGE -> 0.36f
                    }
                )
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
                    setVolume(0.55f, 0.55f)
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
            Sfx.HIT -> 0.75f to 1f
            else -> 0.7f to 1f
        }
        runCatching { pool.play(id, vol, vol, 1, 0, rate) }
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
