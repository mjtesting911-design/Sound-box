package com.example.data.repository

import com.example.data.local.PaymentAlertDao
import com.example.data.model.PaymentAlertEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PaymentRepository(private val dao: PaymentAlertDao) {

    val allAlerts: Flow<List<PaymentAlertEntity>> = dao.getAllAlerts()
    val recentAlerts: Flow<List<PaymentAlertEntity>> = dao.getRecentAlerts()

    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getTodayTotalAmount(): Flow<Double?> = dao.getTotalAmountSince(getStartOfToday())

    fun getTodayCount(): Flow<Int> = dao.getPaymentCountSince(getStartOfToday())

    fun getTodayMaxAmount(): Flow<Double?> = dao.getMaxPaymentSince(getStartOfToday())

    suspend fun insertAlert(alert: PaymentAlertEntity): Long = dao.insertAlert(alert)

    suspend fun deleteAlert(id: Long) = dao.deleteAlertById(id)

    suspend fun clearAll() = dao.clearAll()
}
