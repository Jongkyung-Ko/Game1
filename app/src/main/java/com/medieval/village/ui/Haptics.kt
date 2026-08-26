package com.medieval.village.ui

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 피격 시 짧게 울리는 진동. 진동기가 없는 기기에서는 조용히 무시된다.
 */
class GameHaptics(context: Context) {

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    private val manager: VibratorManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            }.getOrNull()
        } else {
            null
        }

    fun hit(strong: Boolean) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        val durationMs = if (strong) 120L else 45L
        val amplitude = if (strong) VibrationEffect.DEFAULT_AMPLITUDE else 120
        runCatching {
            val effect = VibrationEffect.createOneShot(durationMs, amplitude)
            val mgr = manager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && mgr != null) {
                mgr.vibrate(CombinedVibration.createParallel(effect))
            } else {
                vib.vibrate(effect)
            }
        }
    }
}
