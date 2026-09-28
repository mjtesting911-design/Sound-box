package com.example.service

import java.util.regex.Pattern

data class ParsedPayment(
    val amount: Double,
    val payerName: String,
    val appName: String,
    val appPackage: String,
    val transactionRef: String? = null
)

object PaymentParser {

    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:(?:rs\\.?|inr|₹)\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?))|" +
                "(?:([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)\\s*(?:rs\\.?|inr|₹|rupees))",
        Pattern.CASE_INSENSITIVE
    )

    private val SENDER_PATTERN = Pattern.compile(
        "(?:from|by|sent by|payer)\\s+([A-Za-z0-9\\s]{2,30}?)(?:\\s+(?:to|via|on|ref|using|for|account)|[.,;]|$)",
        Pattern.CASE_INSENSITIVE
    )

    private val REF_PATTERN = Pattern.compile(
        "(?:ref(?:erence)?(?:\\s*no)?|txn(?:\\s*id)?|upi\\s*ref)\\s*[:#-]?\\s*([A-Za-z0-9]{6,25})",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(packageName: String, title: String, text: String, subText: String? = null): ParsedPayment? {
        val combined = "$title $text ${subText ?: ""}".trim()
        val combinedLower = combined.lowercase()

        // Check if this notification is an incoming payment alert
        val isPayment = combinedLower.contains("received") ||
                combinedLower.contains("credited") ||
                combinedLower.contains("paid you") ||
                combinedLower.contains("sent you") ||
                combinedLower.contains("got ₹") ||
                combinedLower.contains("added ₹") ||
                combinedLower.contains("accepted ₹") ||
                combinedLower.contains("payment received") ||
                combinedLower.contains("prapt hue")

        // Exclude debit / money sent notifications (we only announce INCOMING payments)
        val isDebit = (combinedLower.contains("debited") ||
                combinedLower.contains("paid to") ||
                combinedLower.contains("sent to") ||
                combinedLower.contains("transferred to")) &&
                !combinedLower.contains("received") &&
                !combinedLower.contains("credited")

        if (!isPayment || isDebit) {
            return null
        }

        // Extract Amount
        val matcher = AMOUNT_PATTERN.matcher(combined)
        var amount: Double? = null
        if (matcher.find()) {
            val amountStr = (matcher.group(1) ?: matcher.group(2))
                ?.replace(",", "")
                ?.trim()
            amount = amountStr?.toDoubleOrNull()
        }

        if (amount == null || amount <= 0.0) {
            return null
        }

        // Determine App Name
        val appName = resolveAppName(packageName, combinedLower)

        // Extract Sender
        var senderName = "Customer"
        val senderMatcher = SENDER_PATTERN.matcher(combined)
        if (senderMatcher.find()) {
            val candidate = senderMatcher.group(1)?.trim()
            if (!candidate.isNullOrBlank() && candidate.length > 1 && !candidate.equals("you", ignoreCase = true)) {
                senderName = candidate
            }
        }

        // Extract Reference ID
        var refId: String? = null
        val refMatcher = REF_PATTERN.matcher(combined)
        if (refMatcher.find()) {
            refId = refMatcher.group(1)?.trim()
        }

        return ParsedPayment(
            amount = amount,
            payerName = senderName,
            appName = appName,
            appPackage = packageName,
            transactionRef = refId
        )
    }

    private fun resolveAppName(packageName: String, textLower: String): String {
        return when {
            packageName.contains("nbu.paisa") || textLower.contains("google pay") || textLower.contains("gpay") -> "Google Pay"
            packageName.contains("phonepe") || textLower.contains("phonepe") -> "PhonePe"
            packageName.contains("paytm") || textLower.contains("paytm") -> "Paytm"
            packageName.contains("amazon") || textLower.contains("amazon pay") -> "Amazon Pay"
            packageName.contains("npci.upiapp") || textLower.contains("bhim") -> "BHIM UPI"
            packageName.contains("bharatpe") || textLower.contains("bharatpe") -> "BharatPe"
            packageName.contains("dreamplug") || textLower.contains("cred") -> "CRED"
            packageName.contains("sbi") -> "SBI UPI"
            packageName.contains("hdfc") -> "HDFC Bank"
            packageName.contains("icici") -> "ICICI Bank"
            packageName.contains("axis") -> "Axis Bank"
            else -> "UPI Payment"
        }
    }
}
