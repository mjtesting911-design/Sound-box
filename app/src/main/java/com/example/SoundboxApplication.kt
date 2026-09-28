package com.example

import android.app.Application
import com.example.audio.SoundboxAudioManager
import com.example.data.local.AppDatabase
import com.example.data.preferences.SoundboxPreferences
import com.example.data.repository.PaymentRepository

class SoundboxApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { PaymentRepository(database.paymentAlertDao()) }
    val preferences by lazy { SoundboxPreferences(this) }
    val audioManager by lazy { SoundboxAudioManager(this) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        audioManager.destroy()
    }
}
