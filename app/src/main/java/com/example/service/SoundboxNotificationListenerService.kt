package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.SoundboxApplication
import com.example.data.model.PaymentAlertEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SoundboxNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!flat.isNullOrEmpty()) {
                val names = flat.split(":")
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && cn.packageName == pkgName) {
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val app = application as? SoundboxApplication ?: return
        val settings = app.preferences.settingsFlow.value

        // Check if soundbox master toggle is active
        if (!settings.isSoundboxActive) return

        val packageName = sbn.packageName ?: return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val parsed = PaymentParser.parse(packageName, title, text, subText) ?: return

        // Verify if alert is allowed for this specific app
        val isAllowed = when (parsed.appName) {
            "Google Pay" -> settings.allowGPay
            "PhonePe" -> settings.allowPhonePe
            "Paytm" -> settings.allowPaytm
            "BHIM UPI" -> settings.allowBhim
            "Amazon Pay" -> settings.allowAmazonPay
            else -> true
        }

        if (!isAllowed) return

        serviceScope.launch {
            // Save to database
            val entity = PaymentAlertEntity(
                amount = parsed.amount,
                payerName = parsed.payerName,
                appName = parsed.appName,
                appPackage = parsed.appPackage,
                rawMessage = "$title: $text",
                timestamp = System.currentTimeMillis(),
                transactionRef = parsed.transactionRef,
                isTest = false
            )
            app.repository.insertAlert(entity)

            // Announce voice alert via soundbox audio manager
            app.audioManager.announcePayment(
                amount = parsed.amount,
                payerName = parsed.payerName,
                appName = parsed.appName,
                settings = settings
            )
        }
    }
}
