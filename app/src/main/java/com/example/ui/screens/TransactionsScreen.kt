package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentAlertEntity
import com.example.ui.theme.AmazonPayColor
import com.example.ui.theme.BhimColor
import com.example.ui.theme.GPayColor
import com.example.ui.theme.PaytmColor
import com.example.ui.theme.PhonePeColor
import com.example.ui.theme.SoundboxBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SoundboxViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: SoundboxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf("All") } // All, Today, Yesterday, This Week
    var selectedAppFilter by remember { mutableStateOf("All") } // All, Google Pay, PhonePe, Paytm, BHIM, Amazon Pay
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var selectedDetailAlert by remember { mutableStateOf<PaymentAlertEntity?>(null) }

    // Filter calculations
    val now = remember { System.currentTimeMillis() }
    val filteredAlerts = remember(allAlerts, searchQuery, selectedDateFilter, selectedAppFilter) {
        allAlerts.filter { alert ->
            // Search query filter
            val matchesSearch = searchQuery.isBlank() ||
                    alert.payerName.contains(searchQuery, ignoreCase = true) ||
                    alert.appName.contains(searchQuery, ignoreCase = true) ||
                    alert.amount.toString().contains(searchQuery) ||
                    (alert.transactionRef?.contains(searchQuery, ignoreCase = true) == true)

            // Date filter
            val matchesDate = when (selectedDateFilter) {
                "Today" -> isToday(alert.timestamp)
                "Yesterday" -> isYesterday(alert.timestamp)
                "This Week" -> isThisWeek(alert.timestamp)
                else -> true
            }

            // App filter
            val matchesApp = when (selectedAppFilter) {
                "GPay" -> alert.appName.contains("Google", ignoreCase = true)
                "PhonePe" -> alert.appName.contains("PhonePe", ignoreCase = true)
                "Paytm" -> alert.appName.contains("Paytm", ignoreCase = true)
                "BHIM" -> alert.appName.contains("BHIM", ignoreCase = true)
                "Amazon" -> alert.appName.contains("Amazon", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesDate && matchesApp
        }
    }

    val totalFilteredAmount = remember(filteredAlerts) {
        filteredAlerts.sumOf { it.amount }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (allAlerts.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("clear_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_search_input"),
                    placeholder = { Text("Search by payer, amount or app...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dateOptions = listOf("All", "Today", "Yesterday", "This Week")
                    items(dateOptions) { option ->
                        FilterChip(
                            selected = selectedDateFilter == option,
                            onClick = { selectedDateFilter = option },
                            label = { Text(option) },
                            modifier = Modifier.testTag("date_filter_$option")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // App Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val appOptions = listOf("All", "GPay", "PhonePe", "Paytm", "BHIM", "Amazon")
                    items(appOptions) { appOpt ->
                        FilterChip(
                            selected = selectedAppFilter == appOpt,
                            onClick = { selectedAppFilter = appOpt },
                            label = { Text(appOpt) },
                            modifier = Modifier.testTag("app_filter_$appOpt")
                        )
                    }
                }
            }
        }

        // Summary Bar of Filtered results
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredAlerts.size} Payments",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Total: ₹${if (totalFilteredAmount % 1.0 == 0.0) totalFilteredAmount.toInt().toString() else "%.2f".format(totalFilteredAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }

        // Transactions List
        if (filteredAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Transactions Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting filters or simulate an alert from Home screen.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                items(filteredAlerts, key = { it.id }) { alert ->
                    TransactionRowItem(
                        alert = alert,
                        onReplay = { viewModel.replayAlert(alert) },
                        onClick = { selectedDetailAlert = alert }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedDetailAlert?.let { alert ->
        ModalBottomSheet(
            onDismissRequest = { selectedDetailAlert = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            TransactionDetailSheet(
                alert = alert,
                onReplay = { viewModel.replayAlert(alert) },
                onDelete = {
                    viewModel.deleteAlert(alert.id)
                    selectedDetailAlert = null
                },
                onShare = {
                    val shareText = "UPI Payment Receipt\nAmount: ₹${alert.amount}\nPayer: ${alert.payerName}\nApp: ${alert.appName}\nRef: ${alert.transactionRef ?: "N/A"}\nTime: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))}"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                }
            )
        }
    }

    // Clear confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Alerts?") },
            text = { Text("This will permanently remove all transaction voice alert records from your local soundbox history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllAlerts()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_btn")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionRowItem(
    alert: PaymentAlertEntity,
    onReplay: () -> Unit,
    onClick: () -> Unit
) {
    val dateTimeFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(alert.timestamp) { dateTimeFormat.format(Date(alert.timestamp)) }

    val appBadgeColor = when (alert.appName) {
        "Google Pay" -> GPayColor
        "PhonePe" -> PhonePeColor
        "Paytm" -> PaytmColor
        "Amazon Pay" -> AmazonPayColor
        "BHIM UPI" -> BhimColor
        else -> SoundboxBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(appBadgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = alert.appName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = appBadgeColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = alert.payerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${alert.appName} • $formattedDate",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${if (alert.amount % 1.0 == 0.0) alert.amount.toInt().toString() else "%.2f".format(alert.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = SuccessGreen
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onReplay, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay",
                        tint = SoundboxBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionDetailSheet(
    alert: PaymentAlertEntity,
    onReplay: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val fullDateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy 'at' hh:mm:ss a", Locale.getDefault()) }
    val formattedDateTime = remember(alert.timestamp) { fullDateFormat.format(Date(alert.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Payment Alert Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big amount header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Amount Received", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${if (alert.amount % 1.0 == 0.0) alert.amount.toInt().toString() else "%.2f".format(alert.amount)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "via ${alert.appName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(label = "Sender / Customer", value = alert.payerName)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        DetailRow(label = "UPI Application", value = alert.appName)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        DetailRow(label = "Date & Time", value = formattedDateTime)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        DetailRow(label = "Transaction Ref", value = alert.transactionRef ?: "Auto-extracted via Soundbox")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        DetailRow(label = "Type", value = if (alert.isTest) "Manual Sound Test" else "Live Customer Payment")

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = onReplay,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Replay Audio")
            }

            TextButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isThisWeek(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}
