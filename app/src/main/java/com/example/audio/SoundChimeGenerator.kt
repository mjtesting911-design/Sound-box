package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundChimeGenerator {

    suspend fun playChime(
        context: Context,
        chimeType: String,
        vibrate: Boolean = true,
        volumeBoost: Float = 1.0f
    ) = withContext(Dispatchers.IO) {
        if (vibrate) {
            triggerVibration(context)
        }

        if (chimeType == "none") return@withContext

        try {
            val sampleRate = 44100
            val pcmData = when (chimeType) {
                "paytm" -> generatePaytmChime(sampleRate)
                "phonepe" -> generatePhonePeBell(sampleRate)
                "cash_register" -> generateCashRegisterChime(sampleRate)
                "digital" -> generateDigitalPing(sampleRate)
                else -> generatePaytmChime(sampleRate)
            }

            playPcmBuffer(pcmData, sampleRate, volumeBoost)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 120, 80, 200),
                                intArrayOf(0, 180, 0, 255),
                                -1
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(longArrayOf(0, 120, 80, 200), -1)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Iconic Paytm style soundbox chime: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
     */
    private fun generatePaytmChime(sampleRate: Int): ShortArray {
        val noteDurations = doubleArrayOf(0.12, 0.12, 0.12, 0.40)
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        var totalSamples = 0
        for (d in noteDurations) {
            totalSamples += (sampleRate * d).toInt()
        }

        val buffer = ShortArray(totalSamples)
        var offset = 0
        for (i in freqs.indices) {
            val freq = freqs[i]
            val duration = noteDurations[i]
            val count = (sampleRate * duration).toInt()
            val decayRate = if (i == freqs.size - 1) 3.5 else 6.0

            for (j in 0 until count) {
                val t = j.toDouble() / sampleRate
                val envelope = exp(-decayRate * t)
                // Fundamental + rich harmonics
                val sampleVal = (sin(2 * PI * freq * t) * 0.7 +
                        sin(2 * PI * freq * 2 * t) * 0.2 +
                        sin(2 * PI * freq * 3 * t) * 0.1) * envelope

                val shortVal = (sampleVal * 28000).toInt().coerceIn(-32768, 32767)
                if (offset + j < buffer.size) {
                    buffer[offset + j] = shortVal.toShort()
                }
            }
            offset += count
        }
        return buffer
    }

    /**
     * PhonePe style SmartSpeaker chime: High resonance dual bell chime (880Hz & 1320Hz)
     */
    private fun generatePhonePeBell(sampleRate: Int): ShortArray {
        val duration = 0.65
        val count = (sampleRate * duration).toInt()
        val buffer = ShortArray(count)

        for (j in 0 until count) {
            val t = j.toDouble() / sampleRate
            val envelope = exp(-4.2 * t)
            val sampleVal = (sin(2 * PI * 880.0 * t) * 0.5 +
                    sin(2 * PI * 1320.0 * t) * 0.35 +
                    sin(2 * PI * 2640.0 * t) * 0.15) * envelope

            buffer[j] = (sampleVal * 30000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Cash Register Ka-Ching sound
     */
    private fun generateCashRegisterChime(sampleRate: Int): ShortArray {
        val duration = 0.55
        val count = (sampleRate * duration).toInt()
        val buffer = ShortArray(count)

        for (j in 0 until count) {
            val t = j.toDouble() / sampleRate
            val click = if (t < 0.08) sin(2 * PI * 1800.0 * t) * 0.5 else 0.0
            val ring = if (t >= 0.06) {
                val tr = t - 0.06
                (sin(2 * PI * 1568.0 * tr) * 0.6 + sin(2 * PI * 2093.0 * tr) * 0.4) * exp(-5.0 * tr)
            } else 0.0

            val sampleVal = click + ring
            buffer[j] = (sampleVal * 29000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Clean modern digital confirmation ping
     */
    private fun generateDigitalPing(sampleRate: Int): ShortArray {
        val duration = 0.35
        val count = (sampleRate * duration).toInt()
        val buffer = ShortArray(count)

        for (j in 0 until count) {
            val t = j.toDouble() / sampleRate
            val envelope = exp(-6.5 * t)
            val sampleVal = (sin(2 * PI * 987.77 * t) * 0.7 + sin(2 * PI * 1975.5 * t) * 0.3) * envelope
            buffer[j] = (sampleVal * 29000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int, volume: Float) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBufferSize = buffer.size.coerceAtLeast(minBufferSize)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.setVolume(volume.coerceIn(0.1f, 1.0f))
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Wait for audio track playback to finish before speaking TTS
            val playbackDurationMs = ((buffer.size.toDouble() / sampleRate) * 1000).toLong() + 100
            Thread.sleep(playbackDurationMs)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }
}
