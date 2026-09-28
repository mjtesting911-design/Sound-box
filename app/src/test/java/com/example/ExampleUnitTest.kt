package com.example

import com.example.service.PaymentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseGooglePayNotification_isCorrect() {
        val parsed = PaymentParser.parse(
            packageName = "com.google.android.apps.nbu.paisa.user",
            title = "Google Pay",
            text = "Received ₹150.00 from Rahul Sharma"
        )
        assertNotNull(parsed)
        assertEquals(150.0, parsed!!.amount, 0.001)
        assertEquals("Rahul Sharma", parsed.payerName)
        assertEquals("Google Pay", parsed.appName)
    }

    @Test
    fun parsePhonePeNotification_isCorrect() {
        val parsed = PaymentParser.parse(
            packageName = "com.phonepe.app",
            title = "PhonePe Payment Received",
            text = "₹500 credited to your account via PhonePe from Rohit"
        )
        assertNotNull(parsed)
        assertEquals(500.0, parsed!!.amount, 0.001)
        assertEquals("PhonePe", parsed.appName)
    }

    @Test
    fun parsePaytmNotification_isCorrect() {
        val parsed = PaymentParser.parse(
            packageName = "net.one97.paytm",
            title = "Paytm Money Received",
            text = "Received ₹250 in your Paytm wallet from Amit"
        )
        assertNotNull(parsed)
        assertEquals(250.0, parsed!!.amount, 0.001)
        assertEquals("Paytm", parsed.appName)
    }

    @Test
    fun ignoreDebitNotification_isCorrect() {
        val parsed = PaymentParser.parse(
            packageName = "com.google.android.apps.nbu.paisa.user",
            title = "Paid to merchant",
            text = "You paid ₹100 to Chai Point"
        )
        assertNull(parsed)
    }
}
