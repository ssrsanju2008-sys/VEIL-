package com.example.emergency

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

private const val TAG = "EmergencyHapticsService"

class EmergencyHapticsService(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isVibrating = false

    fun startVibration() {
        if (isVibrating || vibrator == null || !vibrator.hasVibrator()) return
        isVibrating = true

        try {
            val timings = longArrayOf(0, 400, 200, 400, 600)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting emergency vibration", e)
        }
    }

    fun stopVibration() {
        if (!isVibrating) return
        isVibrating = false
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration", e)
        }
    }
}
