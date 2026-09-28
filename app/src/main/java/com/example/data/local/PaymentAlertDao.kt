package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentAlertDao {

    @Query("SELECT * FROM payment_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<PaymentAlertEntity>>

    @Query("SELECT * FROM payment_alerts WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getAlertsSince(startTime: Long): Flow<List<PaymentAlertEntity>>

    @Query("SELECT SUM(amount) FROM payment_alerts WHERE timestamp >= :startTime")
    fun getTotalAmountSince(startTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM payment_alerts WHERE timestamp >= :startTime")
    fun getPaymentCountSince(startTime: Long): Flow<Int>

    @Query("SELECT MAX(amount) FROM payment_alerts WHERE timestamp >= :startTime")
    fun getMaxPaymentSince(startTime: Long): Flow<Double?>

    @Query("SELECT * FROM payment_alerts ORDER BY timestamp DESC LIMIT 5")
    fun getRecentAlerts(): Flow<List<PaymentAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PaymentAlertEntity): Long

    @Query("DELETE FROM payment_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Long)

    @Query("DELETE FROM payment_alerts")
    suspend fun clearAll()
}
