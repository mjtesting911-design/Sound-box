package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SoundboxApplication
import com.example.data.model.PaymentAlertEntity
import com.example.data.preferences.SoundboxPreferences
import com.example.service.SoundboxNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundboxViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SoundboxApplication
    private val repository = app.repository
    private val preferences = app.preferences
    private val audioManager = app.audioManager

    val settings: StateFlow<SoundboxPreferences.Settings> = preferences.settingsFlow

    val allAlerts: StateFlow<List<PaymentAlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAlerts: StateFlow<List<PaymentAlertEntity>> = repository.recentAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotal: StateFlow<Double> = repository.getTodayTotalAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayCount: StateFlow<Int> = repository.getTodayCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayMax: StateFlow<Double> = repository.getTodayMaxAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isSpeaking: StateFlow<Boolean> = audioManager.isSpeakingFlow
    val lastAnnounced: StateFlow<String?> = audioManager.lastAnnouncedText

    private val _isNotificationAccessGranted = MutableStateFlow(false)
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    init {
        checkPermission()
    }

    fun checkPermission() {
        val granted = SoundboxNotificationListenerService.isNotificationServiceEnabled(getApplication())
        _isNotificationAccessGranted.value = granted
    }

    fun setSoundboxActive(active: Boolean) {
        preferences.setSoundboxActive(active)
    }

    fun setLanguage(lang: String) {
        preferences.setLanguage(lang)
    }

    fun setChimeType(chime: String) {
        preferences.setChimeType(chime)
    }

    fun setSpeechRate(rate: Float) {
        preferences.setSpeechRate(rate)
    }

    fun setSpeechPitch(pitch: Float) {
        preferences.setSpeechPitch(pitch)
    }

    fun setVolumeBoost(percent: Int) {
        preferences.setVolumeBoost(percent)
    }

    fun setAnnounceSenderName(announce: Boolean) {
        preferences.setAnnounceSenderName(announce)
    }

    fun setVibrate(vibrate: Boolean) {
        preferences.setVibrateOnAlert(vibrate)
    }

    fun setMerchantProfile(upiId: String, businessName: String) {
        preferences.setMerchantProfile(upiId, businessName)
    }

    fun setAppEnabled(appKey: String, enabled: Boolean) {
        preferences.setAppEnabled(appKey, enabled)
    }

    fun triggerTestPayment(amount: Double, payer: String = "Test Customer", appName: String = "Google Pay") {
        viewModelScope.launch {
            val alert = PaymentAlertEntity(
                amount = amount,
                payerName = payer,
                appName = appName,
                appPackage = "com.google.android.apps.nbu.paisa.user",
                rawMessage = "Received ₹$amount from $payer on $appName",
                timestamp = System.currentTimeMillis(),
                transactionRef = "UPI/TEST${(100000..999999).random()}",
                isTest = true
            )
            repository.insertAlert(alert)

            audioManager.announcePayment(
                amount = amount,
                payerName = payer,
                appName = appName,
                settings = settings.value
            )
        }
    }

    fun replayAlert(alert: PaymentAlertEntity) {
        audioManager.announcePayment(
            amount = alert.amount,
            payerName = alert.payerName,
            appName = alert.appName,
            settings = settings.value
        )
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun testCurrentVoice() {
        audioManager.testVoice(settings.value)
    }
}
