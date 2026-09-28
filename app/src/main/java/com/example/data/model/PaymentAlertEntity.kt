package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_alerts")
data class PaymentAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val payerName: String,
    val appName: String,
    val appPackage: String,
    val rawMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionRef: String? = null,
    val isTest: Boolean = false
)
