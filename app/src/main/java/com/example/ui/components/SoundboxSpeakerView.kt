package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SoundboxBlue
import com.example.ui.theme.SoundboxBlueLight
import com.example.ui.theme.SuccessGreen

@Composable
fun SoundboxSpeakerView(
    isActive: Boolean,
    isSpeaking: Boolean,
    onSpeakerTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation when speaking or active
    val infiniteTransition = rememberInfiniteTransition(label = "soundbox_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.28f else if (isActive) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 400 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = if (isSpeaking) 0.8f else 0.4f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 600 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer sound ripple waves when speaking or active
        if (isActive) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = 78.dp.toPx()

                if (isSpeaking) {
                    drawCircle(
                        color = SoundboxBlueLight.copy(alpha = waveAlpha),
                        radius = baseRadius * pulseScale * 1.25f,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = AccentGold.copy(alpha = waveAlpha * 0.7f),
                        radius = baseRadius * pulseScale * 1.1f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = SoundboxBlue.copy(alpha = 0.15f * pulseScale),
                        radius = baseRadius * pulseScale,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Physical Soundbox Terminal Body
        Surface(
            modifier = Modifier
                .size(width = 175.dp, height = 195.dp)
                .shadow(
                    elevation = if (isActive) 18.dp else 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (isSpeaking) SoundboxBlueLight else SoundboxBlue.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(28.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSpeakerTap
                ),
            color = Navy900,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF090D16)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = if (isSpeaking) {
                                listOf(AccentGold, SoundboxBlueLight)
                            } else if (isActive) {
                                listOf(SoundboxBlue, Color(0xFF334155))
                            } else {
                                listOf(Color(0xFF475569), Color(0xFF1E293B))
                            }
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Top status bar of soundbox device: Brand & LED
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MYSOUNDBOX",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.width(28.dp))

                    // Status LED indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .shadow(
                                elevation = if (isActive) 6.dp else 0.dp,
                                shape = CircleShape,
                                spotColor = if (isActive) SuccessGreen else Color.Red
                            )
                            .clip(CircleShape)
                            .background(
                                if (isActive) {
                                    if (isSpeaking) AccentGold else SuccessGreen
                                } else {
                                    Color(0xFFEF4444)
                                }
                            )
                    )
                }

                // Center Speaker Grille Cone
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    Color(0xFF090D16)
                                )
                            )
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    SoundboxBlue,
                                    SoundboxBlueLight,
                                    AccentGold,
                                    SoundboxBlue
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Speaker acoustic mesh pattern
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radiusStep = size.width / 9f
                        for (i in 1..4) {
                            drawCircle(
                                color = if (isSpeaking) SoundboxBlueLight.copy(alpha = 0.5f) else Color(0xFF334155),
                                radius = i * radiusStep,
                                center = center,
                                style = Stroke(
                                    width = 1.2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Golden Center Core with Rupee Symbol ₹
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(
                                elevation = if (isSpeaking) 10.dp else 4.dp,
                                shape = CircleShape,
                                spotColor = AccentGold
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isSpeaking) {
                                        listOf(AccentGold, Color(0xFFFBBF24))
                                    } else {
                                        listOf(Color(0xFFD97706), Color(0xFFB45309))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Bottom speaker wave indicator / label
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Mic,
                        contentDescription = "Sound Status",
                        modifier = Modifier.size(13.dp),
                        tint = if (isSpeaking) AccentGold else if (isActive) SoundboxBlueLight else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSpeaking) "ANNOUNCING..." else if (isActive) "ONLINE" else "STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isSpeaking) AccentGold else if (isActive) SuccessGreen else Color.Gray
                    )
                }
            }
        }
    }
}
