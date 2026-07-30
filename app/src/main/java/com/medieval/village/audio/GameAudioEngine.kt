package com.medieval.village.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

enum class MusicMood {
    VILLAGE,
    COZY,
    TENSE
}

/**
 * 외부 음원 파일 없이 작은 루프 음악과 발소리를 PCM으로 합성한다.
 * AudioTrack은 static mode로 사용해 CPU 사용량을 낮추며 화면 종료 시 반드시 release한다.
 */
class GameAudioEngine {

    private var musicTrack: AudioTrack? = null
    private var footstepTrack: AudioTrack? = null
    private var currentMood: MusicMood? = null
    private var released = false

    fun playMusic(mood: MusicMood) {
        if (released || mood == currentMood) return
        currentMood = mood
        musicTrack?.release()
        musicTrack = createLoop(buildMusic(mood), volume = when (mood) {
            MusicMood.TENSE -> 0.20f
            MusicMood.COZY -> 0.16f
            MusicMood.VILLAGE -> 0.14f
        })?.also { it.play() }
    }

    fun setWalking(walking: Boolean) {
        if (released) return
        if (!walking) {
            footstepTrack?.pause()
            footstepTrack?.flush()
            return
        }
        if (footstepTrack == null) {
            footstepTrack = createLoop(buildFootsteps(), volume = 0.24f)
        }
        if (footstepTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
            footstepTrack?.play()
        }
    }

    fun pause() {
        musicTrack?.pause()
        footstepTrack?.pause()
    }

    fun resume() {
        if (!released && currentMood != null) musicTrack?.play()
    }

    fun release() {
        released = true
        musicTrack?.release()
        footstepTrack?.release()
        musicTrack = null
        footstepTrack = null
    }

    private fun createLoop(samples: ShortArray, volume: Float): AudioTrack? {
        return runCatching {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            track.write(samples, 0, samples.size)
            track.setLoopPoints(0, samples.size, -1)
            track.setVolume(volume)
            track
        }.getOrNull()
    }

    private fun buildMusic(mood: MusicMood): ShortArray {
        val beats = 16
        val secondsPerBeat = when (mood) {
            MusicMood.VILLAGE -> 0.46
            MusicMood.COZY -> 0.58
            MusicMood.TENSE -> 0.34
        }
        val total = (SAMPLE_RATE * beats * secondsPerBeat).toInt()
        val output = ShortArray(total)
        val melody = when (mood) {
            MusicMood.VILLAGE -> intArrayOf(64, 67, 69, 67, 64, 62, 60, 62, 64, 67, 72, 69, 67, 64, 62, 60)
            MusicMood.COZY -> intArrayOf(60, 64, 67, 64, 59, 62, 67, 62, 60, 64, 69, 67, 64, 62, 60, 55)
            MusicMood.TENSE -> intArrayOf(45, 45, 48, 46, 45, 52, 48, 46, 45, 45, 53, 52, 48, 46, 45, 43)
        }
        val roots = when (mood) {
            MusicMood.VILLAGE -> intArrayOf(48, 53, 55, 48)
            MusicMood.COZY -> intArrayOf(48, 45, 53, 43)
            MusicMood.TENSE -> intArrayOf(33, 34, 31, 33)
        }

        for (i in output.indices) {
            val time = i.toDouble() / SAMPLE_RATE
            val beatPosition = time / secondsPerBeat
            val beat = beatPosition.toInt().coerceIn(0, beats - 1)
            val phase = beatPosition - beat
            val envelope = (1.0 - phase).coerceAtLeast(0.0)
            val melodyFrequency = midiToHz(melody[beat])
            val rootFrequency = midiToHz(roots[(beat / 4) % roots.size])
            val melodyWave = triangle(time * melodyFrequency) * envelope
            val drone = sin(2.0 * PI * rootFrequency * time) * 0.42
            val fifth = sin(2.0 * PI * rootFrequency * 1.5 * time) * 0.18
            val pulse = if (mood == MusicMood.TENSE && phase < 0.11) {
                sin(2.0 * PI * 82.0 * time) * (1.0 - phase / 0.11) * 0.55
            } else {
                0.0
            }
            output[i] = ((melodyWave * 0.48 + drone + fifth + pulse) * Short.MAX_VALUE * 0.55)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return output
    }

    private fun buildFootsteps(): ShortArray {
        val duration = 0.56
        val total = (SAMPLE_RATE * duration).toInt()
        val output = ShortArray(total)
        var noise = 0x1234567
        for (i in output.indices) {
            val time = i.toDouble() / SAMPLE_RATE
            val local = when {
                time < 0.08 -> time
                time in 0.28..0.36 -> time - 0.28
                else -> -1.0
            }
            if (local >= 0.0) {
                noise = noise * 1103515245 + 12345
                val rough = ((noise ushr 16) and 0x7fff) / 16384.0 - 1.0
                val envelope = (1.0 - local / 0.08).coerceAtLeast(0.0)
                val thump = sin(2.0 * PI * 72.0 * local) * 0.65
                output[i] = ((rough * 0.35 + thump) * envelope * Short.MAX_VALUE * 0.42).toInt().toShort()
            }
        }
        return output
    }

    private fun triangle(cycles: Double): Double {
        val fraction = cycles - kotlin.math.floor(cycles)
        return 1.0 - 4.0 * kotlin.math.abs(fraction - 0.5)
    }

    private fun midiToHz(note: Int): Double = 440.0 * 2.0.pow((note - 69) / 12.0)

    private fun Double.pow(exponent: Double): Double = Math.pow(this, exponent)

    private companion object {
        const val SAMPLE_RATE = 22_050
    }
}
