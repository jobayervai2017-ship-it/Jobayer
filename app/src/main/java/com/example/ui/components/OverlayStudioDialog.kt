package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.OverlayImageItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun OverlayStudioDialog(
    overlay: OverlayImageItem?,
    onDismiss: () -> Unit,
    onAddNewOverlay: () -> Unit,
    onUpdateProperties: (Float, Float, Float, Float, Float) -> Unit, // posX, posY, scale, rotation, opacity
    onUpdateTiming: (Double, Double) -> Unit,
    onNudge: (Double) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("overlay_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ওভারলে / PIP স্টুডিও (Multi-Layer)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    if (overlay != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Add Another Overlay button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentPurple.copy(alpha = 0.2f))
                        .clickable { onAddNewOverlay() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ নতুন ওভারলে / স্টিকার / লোগো যুক্ত করুন", fontSize = 12.sp, color = AccentPurple, fontWeight = FontWeight.Bold)
                    }
                }

                if (overlay != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    var scale by remember { mutableFloatStateOf(overlay.scale) }
                    var opacity by remember { mutableFloatStateOf(overlay.opacity) }
                    var rotation by remember { mutableFloatStateOf(overlay.rotation) }
                    var posX by remember { mutableFloatStateOf(overlay.posX) }
                    var posY by remember { mutableFloatStateOf(overlay.posY) }
                    var startSec by remember { mutableFloatStateOf(overlay.startSeconds.toFloat()) }
                    var durSec by remember { mutableFloatStateOf(overlay.durationSeconds.toFloat()) }

                    // Scale
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("সাইজ / স্কেল (Scale):", color = TextSecondary, fontSize = 12.sp)
                        Text("${(scale * 100).toInt()}%", fontWeight = FontWeight.Bold, color = AccentYellow, fontSize = 12.sp)
                    }
                    Slider(
                        value = scale,
                        onValueChange = {
                            scale = it
                            onUpdateProperties(posX, posY, it, rotation, opacity)
                        },
                        valueRange = 0.1f..1.8f,
                        colors = SliderDefaults.colors(thumbColor = AccentYellow, activeTrackColor = AccentYellow)
                    )

                    // Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("স্বচ্ছতা (Opacity):", color = TextSecondary, fontSize = 12.sp)
                        Text("${(opacity * 100).toInt()}%", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 12.sp)
                    }
                    Slider(
                        value = opacity,
                        onValueChange = {
                            opacity = it
                            onUpdateProperties(posX, posY, scale, rotation, it)
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )

                    // Position X & Y
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("পজিশন X ও Y (বা স্ক্রিনে সরাসরি ড্র্যাগ করুন):", color = TextSecondary, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("X: ${(posX * 100).toInt()}%", fontSize = 10.sp, color = TextMuted)
                            Slider(
                                value = posX,
                                onValueChange = {
                                    posX = it
                                    onUpdateProperties(it, posY, scale, rotation, opacity)
                                },
                                valueRange = 0.05f..0.95f
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Y: ${(posY * 100).toInt()}%", fontSize = 10.sp, color = TextMuted)
                            Slider(
                                value = posY,
                                onValueChange = {
                                    posY = it
                                    onUpdateProperties(posX, it, scale, rotation, opacity)
                                },
                                valueRange = 0.05f..0.95f
                            )
                        }
                    }

                    // Move Start Position (Nudge)
                    Text(
                        text = "টাইমলাইনে ওভারলে পজিশন সরান (Timeline Nudge):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { onNudge(-1.0) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("<< -1s", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { onNudge(-0.2) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("< -0.2s", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { onNudge(0.2) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+0.2s >", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { onNudge(1.0) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+1s >>", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDuplicate,
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duplicate", color = TextPrimary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", color = TextSecondary)
                    }
                }
            }
        }
    }
}
