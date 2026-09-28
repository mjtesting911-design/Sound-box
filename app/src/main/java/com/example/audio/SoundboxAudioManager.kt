package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.preferences.SoundboxPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class SoundboxAudioManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isSpeakingFlow = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    private val _lastAnnouncedText = MutableStateFlow<String?>(null)
    val lastAnnouncedText: StateFlow<String?> = _lastAnnouncedText.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeakingFlow.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeakingFlow.value = false
                    abandonFocus()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeakingFlow.value = false
                    abandonFocus()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeakingFlow.value = false
                    abandonFocus()
                }
            })
        }
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).build()
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun announcePayment(
        amount: Double,
        payerName: String,
        appName: String,
        settings: SoundboxPreferences.Settings,
        onComplete: (() -> Unit)? = null
    ) {
        scope.launch(Dispatchers.IO) {
            _isSpeakingFlow.value = true

            // Boost stream volume if requested
            if (settings.volumeBoostPercent > 0) {
                try {
                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val targetVol = (maxVol * (settings.volumeBoostPercent / 100f)).toInt().coerceIn(1, maxVol)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 1. Play Chime
            SoundChimeGenerator.playChime(
                context = context,
                chimeType = settings.chimeType,
                vibrate = settings.vibrateOnAlert,
                volumeBoost = settings.volumeBoostPercent / 100f
            )

            // 2. Speak Voice Message
            val phrase = buildAnnouncementPhrase(
                amount = amount,
                payerName = payerName,
                appName = appName,
                langCode = settings.languageCode,
                announceSender = settings.announceSenderName
            )
            _lastAnnouncedText.value = phrase

            speakText(
                text = phrase,
                langCode = settings.languageCode,
                pitch = settings.speechPitch,
                rate = settings.speechRate
            )

            onComplete?.invoke()
        }
    }

    fun testVoice(settings: SoundboxPreferences.Settings) {
        announcePayment(
            amount = 100.0,
            payerName = "Rahul",
            appName = "Google Pay",
            settings = settings
        )
    }

    private fun speakText(text: String, langCode: String, pitch: Float, rate: Float) {
        if (!isTtsInitialized || tts == null) {
            initTts()
        }

        requestFocus()

        val locale = getLocaleForCode(langCode)
        tts?.language = locale
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        val utteranceId = "soundbox_alert_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun getLocaleForCode(langCode: String): Locale {
        return when (langCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "te" -> Locale("te", "IN")
            "ta" -> Locale("ta", "IN")
            "mr" -> Locale("mr", "IN")
            "gu" -> Locale("gu", "IN")
            "kn" -> Locale("kn", "IN")
            else -> Locale("en", "IN")
        }
    }

    fun buildAnnouncementPhrase(
        amount: Double,
        payerName: String,
        appName: String,
        langCode: String,
        announceSender: Boolean
    ): String {
        val amountStr = if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", amount)
        }

        val hasSender = announceSender && payerName.isNotBlank() && payerName != "Customer"

        return when (langCode) {
            "hi" -> {
                if (hasSender) {
                    "$payerName से $appName पर $amountStr रुपये प्राप्त हुए"
                } else {
                    "$appName पर $amountStr रुपये प्राप्त हुए"
                }
            }
            "bn" -> {
                if (hasSender) {
                    "$payerName এর কাছ থেকে $appName-এ $amountStr টাকা পাওয়া গেছে"
                } else {
                    "$appName-এ $amountStr টাকা পাওয়া গেছে"
                }
            }
            "te" -> {
                if (hasSender) {
                    "$payerName నుండి $appName లో $amountStr రూపాయలు వచ్చాయి"
                } else {
                    "$appName లో $amountStr రూపాయలు వచ్చాయి"
                }
            }
            "ta" -> {
                if (hasSender) {
                    "$payerName இடமிருந்து $appName இல் $amountStr ரூபாய் பெறப்பட்டது"
                } else {
                    "$appName இல் $amountStr ரூபாய் பெறப்பட்டது"
                }
            }
            "mr" -> {
                if (hasSender) {
                    "$payerName कडून $appName वर $amountStr रुपये प्राप्त झाले"
                } else {
                    "$appName वर $amountStr रुपये प्राप्त झाले"
                }
            }
            "gu" -> {
                if (hasSender) {
                    "$payerName તરફથી $appName પર $amountStr રૂપિયા મળ્યા"
                } else {
                    "$appName પર $amountStr રૂપિયા મળ્યા"
                }
            }
            "kn" -> {
                if (hasSender) {
                    "$payerName ರಿಂದ $appName ನಲ್ಲಿ $amountStr ರೂಪಾಯಿ ಸ್ವೀಕರಿಸಲಾಗಿದೆ"
                } else {
                    "$appName ನಲ್ಲಿ $amountStr ರೂಪಾಯಿ ಸ್ವೀಕರಿಸಲಾಗಿದೆ"
                }
            }
            else -> { // Default English
                if (hasSender) {
                    "Received $amountStr rupees from $payerName on $appName"
                } else {
                    "Received $amountStr rupees on $appName"
                }
            }
        }
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
