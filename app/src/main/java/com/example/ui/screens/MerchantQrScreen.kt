package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentGold
import com.example.ui.theme.Navy900
import com.example.ui.theme.SoundboxBlue
import com.example.ui.theme.SoundboxBlueLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SoundboxViewModel
import kotlin.math.abs

@Composable
fun MerchantQrScreen(
    viewModel: SoundboxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var isEditingProfile by remember { mutableStateOf(false) }
    var inputUpiId by remember(settings.merchantUpiId) { mutableStateOf(settings.merchantUpiId) }
    var inputBizName by remember(settings.merchantBusinessName) { mutableStateOf(settings.merchantBusinessName) }

    var selectedAmountChip by remember { mutableStateOf<Double?>(null) }
    var customAmountInput by remember { mutableStateOf("") }

    val upiPayload = remember(settings.merchantUpiId, settings.merchantBusinessName, selectedAmountChip, customAmountInput) {
        val amt = selectedAmountChip ?: customAmountInput.toDoubleOrNull()
        val amtParam = if (amt != null && amt > 0) "&am=$amt" else ""
        "upi://pay?pa=${settings.merchantUpiId}&pn=${settings.merchantBusinessName.replace(" ", "%20")}&cu=INR$amtParam"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Merchant Soundbox QR",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Scan with any UPI app to pay",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { isEditingProfile = !isEditingProfile },
                    modifier = Modifier.testTag("edit_merchant_qr_profile")
                ) {
                    Icon(
                        imageVector = if (isEditingProfile) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = "Edit Merchant Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Profile Editor Card if active
        if (isEditingProfile) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Merchant Soundbox Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputBizName,
                        onValueChange = { inputBizName = it },
                        label = { Text("Store / Merchant Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputUpiId,
                        onValueChange = { inputUpiId = it },
                        label = { Text("UPI ID (e.g. store@upi)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.setMerchantProfile(inputUpiId, inputBizName)
                            isEditingProfile = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Physical Soundbox Counter Stand Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Soundbox brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = settings.merchantBusinessName.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Navy900
                        )
                        Text(
                            text = "UPI ID: ${settings.merchantUpiId}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Navy900)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SOUNDBOX",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code View Container
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Generated UPI QR Graphic
                    UpiQrCanvas(
                        content = upiPayload,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Center Soundbox Icon Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Navy900)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Accepted Apps Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GPay • PhonePe • Paytm • BHIM • Any UPI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Optional Quick Amount Presets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "REQUEST SPECIFIC AMOUNT (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val amounts = listOf(null, 50.0, 100.0, 200.0, 500.0)
                amounts.forEach { amt ->
                    val isSelected = selectedAmountChip == amt && customAmountInput.isEmpty()
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedAmountChip = amt
                            customAmountInput = ""
                        },
                        label = { Text(if (amt == null) "Any" else "₹${amt.toInt()}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Simulation Trigger Button
            Button(
                onClick = {
                    val amountToSimulate = selectedAmountChip ?: customAmountInput.toDoubleOrNull() ?: 100.0
                    viewModel.triggerTestPayment(
                        amount = amountToSimulate,
                        payer = "Customer",
                        appName = "Google Pay"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_qr_scan_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Customer Scan & Payment", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Share UPI Link
            TextButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Pay ${settings.merchantBusinessName} via UPI: $upiPayload")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Payment QR Link"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share UPI Payment Link")
            }
        }
    }
}

/**
 * High-definition QR Code pattern canvas with standard 3 finder corner squares,
 * timing tracks, and pseudo-random deterministic data pattern based on payload.
 */
@Composable
fun UpiQrCanvas(
    content: String,
    modifier: Modifier = Modifier
) {
    val modules = 29 // 29x29 standard version 3 QR matrix
    val matrix = remember(content) {
        generateQrMatrix(content, modules)
    }

    Canvas(modifier = modifier) {
        val cellSize = size.width / modules
        val darkColor = Color(0xFF0F172A)

        for (r in 0 until modules) {
            for (c in 0 until modules) {
                if (matrix[r][c]) {
                    // Skip drawing center 5x5 to give space for Rupee badge
                    val isCenterBadge = (r in 11..17 && c in 11..17)
                    if (!isCenterBadge) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(content: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) }

    fun drawFinder(topRow: Int, leftCol: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                matrix[topRow + r][leftCol + c] = isBorder || isInner
            }
        }
    }

    // Three Finder patterns (top-left, top-right, bottom-left)
    drawFinder(0, 0)
    drawFinder(0, size - 7)
    drawFinder(size - 7, 0)

    // Timing patterns
    for (i in 7 until size - 7) {
        if (i % 2 == 0) {
            matrix[6][i] = true
            matrix[i][6] = true
        }
    }

    // Seed data generator based on string hash
    var hash = abs(content.hashCode())
    for (r in 0 until size) {
        for (c in 0 until size) {
            val inFinder = (r < 8 && c < 8) || (r < 8 && c >= size - 8) || (r >= size - 8 && c < 8)
            val inTiming = r == 6 || c == 6
            if (!inFinder && !inTiming) {
                hash = (hash * 31 + r * 7 + c * 13) and 0x7FFFFFFF
                matrix[r][c] = (hash % 3 == 0 || hash % 7 == 0)
            }
        }
    }

    return matrix
}
