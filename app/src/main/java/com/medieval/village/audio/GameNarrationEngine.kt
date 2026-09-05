package com.medieval.village.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * 인트로 등 한국어 나레이션. 기기에 한국어 TTS가 있으면 읽고, 없으면 조용히 넘어간다.
 */
class GameNarrationEngine(context: Context) {

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var ready = false
    private var released = false
    private var pending: String? = null
    private var volume = 1f

    init {
        tts = TextToSpeech(appContext) { status ->
            main.post {
                if (released) return@post
                if (status != TextToSpeech.SUCCESS) return@post
                val engine = tts ?: return@post
                val korean = engine.setLanguage(Locale.KOREAN)
                if (korean == TextToSpeech.LANG_MISSING_DATA || korean == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.getDefault())
                }
                engine.setSpeechRate(0.92f)
                engine.setPitch(0.98f)
                engine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                ready = true
                pending?.let { speakNow(it) }
            }
        }
    }

    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
    }

    fun speak(text: String) {
        if (released) return
        if (!ready) {
            pending = text
            return
        }
        speakNow(text)
    }

    private fun speakNow(text: String) {
        pending = null
        val engine = tts ?: return
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
        }
        runCatching {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "prologue-narration")
        }
    }

    fun stop() {
        pending = null
        runCatching { tts?.stop() }
    }

    fun release() {
        released = true
        pending = null
        ready = false
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
    }
}
