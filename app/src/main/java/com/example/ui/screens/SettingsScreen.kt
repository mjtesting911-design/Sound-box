package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmazonPayColor
import com.example.ui.theme.BhimColor
import com.example.ui.theme.GPayColor
import com.example.ui.theme.PaytmColor
import com.example.ui.theme.PhonePeColor
import com.example.ui.theme.SoundboxBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SoundboxViewModel

data class LanguageOption(val code: String, val name: String, val nativeName: String)

@Composable
fun SettingsScreen(
    viewModel: SoundboxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.isNotificationAccessGranted.collectAsStateWithLifecycle()

    val languages = remember {
        listOf(
            LanguageOption("hi", "Hindi", "हिन्दी"),
            LanguageOption("en", "English", "English"),
            LanguageOption("bn", "Bengali", "বাংলা"),
            LanguageOption("te", "Telugu", "తెలుగు"),
            LanguageOption("ta", "Tamil", "தமிழ்"),
            LanguageOption("mr", "Marathi", "मराठी"),
            LanguageOption("gu", "Gujarati", "ગુજરાતી"),
            LanguageOption("kn", "Kannada", "ಕನ್ನಡ")
        )
    }

    val chimeOptions = remember {
        listOf(
            "paytm" to "Paytm Chime",
            "phonepe" to "PhonePe Bell",
            "cash_register" to "Cash Ka-Ching",
            "digital" to "Digital Ping",
            "none" to "No Chime"
        )
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Soundbox Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize voice, languages, chimes & alerts",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Test Voice Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Current Soundbox Voice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Plays: ${chimeOptions.find { it.first == settings.chimeType }?.second ?: ""} + Voice Alert",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = { viewModel.testCurrentVoice() },
                        modifier = Modifier.testTag("test_voice_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Listen")
                    }
                }
            }
        }

        // 1. Language Selection
        item {
            SectionHeader(icon = Icons.Default.Language, title = "ANNOUNCEMENT LANGUAGE")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select speech language for payment announcements:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(languages) { lang ->
                            val isSelected = settings.languageCode == lang.code
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setLanguage(lang.code) },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = lang.nativeName, fontWeight = FontWeight.Bold)
                                        Text(text = lang.name, fontSize = 10.sp)
                                    }
                                },
                                modifier = Modifier.testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        // 2. Chime Style Selection
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(icon = Icons.Default.GraphicEq, title = "ALERT CHIME SOUND")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Played right before voice announcement:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chimeOptions) { chime ->
                            FilterChip(
                                selected = settings.chimeType == chime.first,
                                onClick = { viewModel.setChimeType(chime.first) },
                                label = { Text(chime.second) },
                                modifier = Modifier.testTag("chime_chip_${chime.first}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Speech Tuning (Speed, Pitch, Volume)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(icon = Icons.Default.Tune, title = "VOICE ENGINE & VOLUME")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Speech Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Speech Rate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "%.1fx".format(settings.speechRate), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.speechRate,
                        onValueChange = { viewModel.setSpeechRate(it) },
                        valueRange = 0.7f..1.4f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Speech Pitch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Voice Pitch", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "%.1fx".format(settings.speechPitch), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.speechPitch,
                        onValueChange = { viewModel.setSpeechPitch(it) },
                        valueRange = 0.7f..1.3f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Volume Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Soundbox Volume Boost", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "${settings.volumeBoostPercent}%", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.volumeBoostPercent.toFloat(),
                        onValueChange = { viewModel.setVolumeBoost(it.toInt()) },
                        valueRange = 20f..100f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Announce Sender Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Announce Payer Name", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = "Say sender's name (e.g. Received from Rohan)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.announceSenderName,
                            onCheckedChange = { viewModel.setAnnounceSenderName(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Vibrate Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Vibrate on Payment", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = "Dual haptic pulse when payment arrives", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.vibrateOnAlert,
                            onCheckedChange = { viewModel.setVibrate(it) }
                        )
                    }
                }
            }
        }

        // 4. Supported Apps Filters
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(icon = Icons.Default.Notifications, title = "SUPPORTED UPI APPS")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AppToggleRow(name = "Google Pay (GPay)", color = GPayColor, enabled = settings.allowGPay) {
                        viewModel.setAppEnabled("gpay", it)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AppToggleRow(name = "PhonePe", color = PhonePeColor, enabled = settings.allowPhonePe) {
                        viewModel.setAppEnabled("phonepe", it)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AppToggleRow(name = "Paytm", color = PaytmColor, enabled = settings.allowPaytm) {
                        viewModel.setAppEnabled("paytm", it)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AppToggleRow(name = "BHIM UPI", color = BhimColor, enabled = settings.allowBhim) {
                        viewModel.setAppEnabled("bhim", it)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AppToggleRow(name = "Amazon Pay", color = AmazonPayColor, enabled = settings.allowAmazonPay) {
                        viewModel.setAppEnabled("amazon_pay", it)
                    }
                }
            }
        }

        // 5. System Permissions & Battery Optimization
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(icon = Icons.Default.Security, title = "SYSTEM & PERMISSIONS")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Notification Access Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Notification Access", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (isPermissionGranted) "Granted • Soundbox is actively listening" else "Not Granted • Tap to allow",
                                fontSize = 11.sp,
                                color = if (isPermissionGranted) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Text(if (isPermissionGranted) "Manage" else "Enable")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Battery Optimization ignore
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Background Battery Lock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "Keep Soundbox alive even when screen is locked",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Text("Configure")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Bluetooth Speaker Pro-Tip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = SoundboxBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tip: Pair any Bluetooth speaker with this phone to make payment announcements loud across your whole store!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AppToggleRow(
    name: String,
    color: Color,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}
