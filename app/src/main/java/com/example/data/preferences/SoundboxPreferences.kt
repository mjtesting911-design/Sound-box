package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SoundboxPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("soundbox_settings_prefs", Context.MODE_PRIVATE)

    data class Settings(
        val isSoundboxActive: Boolean = true,
        val languageCode: String = "hi", // Default Hindi as in India soundboxes, can be English or Bengali etc.
        val chimeType: String = "paytm", // paytm, phonepe, cash_register, digital, none
        val speechRate: Float = 1.0f,
        val speechPitch: Float = 1.0f,
        val volumeBoostPercent: Int = 100, // 0 to 100%
        val announceSenderName: Boolean = true,
        val vibrateOnAlert: Boolean = true,
        val merchantUpiId: String = "merchant@upi",
        val merchantBusinessName: String = "My Store",
        val allowGPay: Boolean = true,
        val allowPhonePe: Boolean = true,
        val allowPaytm: Boolean = true,
        val allowBhim: Boolean = true,
        val allowAmazonPay: Boolean = true
    )

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): Settings {
        return Settings(
            isSoundboxActive = prefs.getBoolean("is_soundbox_active", true),
            languageCode = prefs.getString("language_code", "hi") ?: "hi",
            chimeType = prefs.getString("chime_type", "paytm") ?: "paytm",
            speechRate = prefs.getFloat("speech_rate", 1.0f),
            speechPitch = prefs.getFloat("speech_pitch", 1.0f),
            volumeBoostPercent = prefs.getInt("volume_boost", 100),
            announceSenderName = prefs.getBoolean("announce_sender", true),
            vibrateOnAlert = prefs.getBoolean("vibrate_on_alert", true),
            merchantUpiId = prefs.getString("merchant_upi_id", "merchant@upi") ?: "merchant@upi",
            merchantBusinessName = prefs.getString("merchant_biz_name", "My Store") ?: "My Store",
            allowGPay = prefs.getBoolean("allow_gpay", true),
            allowPhonePe = prefs.getBoolean("allow_phonepe", true),
            allowPaytm = prefs.getBoolean("allow_paytm", true),
            allowBhim = prefs.getBoolean("allow_bhim", true),
            allowAmazonPay = prefs.getBoolean("allow_amazon_pay", true)
        )
    }

    fun setSoundboxActive(active: Boolean) {
        prefs.edit().putBoolean("is_soundbox_active", active).apply()
        _settingsFlow.value = _settingsFlow.value.copy(isSoundboxActive = active)
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language_code", lang).apply()
        _settingsFlow.value = _settingsFlow.value.copy(languageCode = lang)
    }

    fun setChimeType(chime: String) {
        prefs.edit().putString("chime_type", chime).apply()
        _settingsFlow.value = _settingsFlow.value.copy(chimeType = chime)
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat("speech_rate", rate).apply()
        _settingsFlow.value = _settingsFlow.value.copy(speechRate = rate)
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat("speech_pitch", pitch).apply()
        _settingsFlow.value = _settingsFlow.value.copy(speechPitch = pitch)
    }

    fun setVolumeBoost(percent: Int) {
        prefs.edit().putInt("volume_boost", percent).apply()
        _settingsFlow.value = _settingsFlow.value.copy(volumeBoostPercent = percent)
    }

    fun setAnnounceSenderName(announce: Boolean) {
        prefs.edit().putBoolean("announce_sender", announce).apply()
        _settingsFlow.value = _settingsFlow.value.copy(announceSenderName = announce)
    }

    fun setVibrateOnAlert(vibrate: Boolean) {
        prefs.edit().putBoolean("vibrate_on_alert", vibrate).apply()
        _settingsFlow.value = _settingsFlow.value.copy(vibrateOnAlert = vibrate)
    }

    fun setMerchantProfile(upiId: String, businessName: String) {
        prefs.edit()
            .putString("merchant_upi_id", upiId)
            .putString("merchant_biz_name", businessName)
            .apply()
        _settingsFlow.value = _settingsFlow.value.copy(
            merchantUpiId = upiId,
            merchantBusinessName = businessName
        )
    }

    fun setAppEnabled(appKey: String, enabled: Boolean) {
        prefs.edit().putBoolean("allow_$appKey", enabled).apply()
        _settingsFlow.value = when (appKey) {
            "gpay" -> _settingsFlow.value.copy(allowGPay = enabled)
            "phonepe" -> _settingsFlow.value.copy(allowPhonePe = enabled)
            "paytm" -> _settingsFlow.value.copy(allowPaytm = enabled)
            "bhim" -> _settingsFlow.value.copy(allowBhim = enabled)
            "amazon_pay" -> _settingsFlow.value.copy(allowAmazonPay = enabled)
            else -> _settingsFlow.value
        }
    }
}
