package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AnimationPreset
import com.example.model.AssPresetStyle
import com.example.model.ColorPalette
import com.example.model.TextEffectPreset
import com.example.model.TextSyncItem
import com.example.model.TypographyList
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubtitleStyleDialog(
    subtitle: TextSyncItem?,
    onDismiss: () -> Unit,
    onApplyToItem: (TextSyncItem) -> Unit,
    onApplyPresetToAll: (AssPresetStyle) -> Unit
) {
    if (subtitle == null) return

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFont by remember { mutableStateOf(subtitle.fontName) }
    var selectedFontSize by remember { mutableIntStateOf(subtitle.fontSize) }
    var selectedTextColor by remember { mutableStateOf(subtitle.textColorHex) }
    var selectedOutlineColor by remember { mutableStateOf(subtitle.outlineColorHex) }
    var selectedAnimation by remember { mutableStateOf(subtitle.animationPreset) }
    var selectedEffect by remember { mutableStateOf(subtitle.textEffect) }

    val tabs = listOf(
        "Templates (30+)",
        "Animations (50+)",
        "Fonts (50+)",
        "Effects (26+)",
        "Colors (60+)",
        "Size & Style"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .testTag("subtitle_style_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ক্যাপকাট টেক্সট ও সাবটাইটেল স্টুডিও",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Live Preview Card
                val previewColor = try { Color(android.graphics.Color.parseColor(selectedTextColor)) } catch (_: Exception) { Color.Yellow }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subtitle.text.ifEmpty { "PREVIEW VIRAL TEXT" },
                            fontSize = (selectedFontSize * 0.55).coerceIn(16.0, 32.0).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = previewColor
                        )
                    }
                }

                // Category Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurfaceElevated,
                    contentColor = AccentPurple,
                    edgePadding = 4.dp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) AccentPurple else TextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content Body
                Box(modifier = Modifier.weight(1f, fill = false).height(300.dp)) {
                    when (selectedTab) {
                        // TAB 0: 30+ Viral Templates
                        0 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(AssPresetStyle.ALL_PRESETS) { preset ->
                                    val isMatch = selectedFont == preset.fontName &&
                                            selectedTextColor == preset.textColorHex &&
                                            selectedAnimation == preset.animationPreset
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedFont = preset.fontName
                                                selectedFontSize = preset.fontSize
                                                selectedTextColor = preset.textColorHex
                                                selectedOutlineColor = preset.outlineColorHex
                                                selectedAnimation = preset.animationPreset
                                                selectedEffect = preset.textEffect
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMatch) AccentPurple.copy(alpha = 0.25f) else DarkSurfaceVariant
                                        ),
                                        border = if (isMatch) androidx.compose.foundation.BorderStroke(1.5.dp, AccentPurple) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                Text(preset.description, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
                                            }
                                            Button(
                                                onClick = {
                                                    onApplyPresetToAll(preset)
                                                    onDismiss()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                                modifier = Modifier.height(28.dp).padding(start = 6.dp)
                                            ) {
                                                Text("All", fontSize = 10.sp, color = AccentCyan)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 1: 50+ Animations
                        1 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(AnimationPreset.entries) { anim ->
                                    val isSelected = selectedAnimation == anim
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedAnimation = anim },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) AccentCyan.copy(alpha = 0.25f) else DarkSurfaceVariant
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(anim.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                Text(anim.category.label, fontSize = 10.sp, color = AccentYellow)
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 2: 50+ Fonts
                        2 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(TypographyList.ALL_FONTS) { font ->
                                    val isSelected = selectedFont == font
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedFont = font },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) AccentPurple.copy(alpha = 0.3f) else DarkSurfaceVariant
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentPurple) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(font, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 3: 26+ Text Effects
                        3 -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(TextEffectPreset.entries) { effect ->
                                    val isSelected = selectedEffect == effect
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedEffect = effect },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) AccentYellow.copy(alpha = 0.25f) else DarkSurfaceVariant
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(effect.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 4: 60+ Colors Palette
                        4 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("টেক্সট কালার (Text Fill Color):", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ColorPalette.SWATCHES.forEach { hex ->
                                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.White }
                                        val isSelected = selectedTextColor.equals(hex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White else DarkSurfaceElevated, CircleShape)
                                                .clickable { selectedTextColor = hex },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = if (hex == "#FFFFFF") Color.Black else Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("আউটলাইন কালার (Outline / Stroke Color):", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("#000000", "#FFFFFF", "#FFE600", "#00F0FF", "#39FF14", "#FF007F", "#6366F1", "#1E293B", "#FF4500").forEach { hex ->
                                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black }
                                        val isSelected = selectedOutlineColor.equals(hex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) AccentCyan else DarkSurfaceElevated, CircleShape)
                                                .clickable { selectedOutlineColor = hex },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = if (hex == "#FFFFFF") Color.Black else Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 5: Size & Box Style
                        5 -> {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ফন্ট সাইজ (Font Size):", fontSize = 12.sp, color = TextSecondary)
                                    Text("${selectedFontSize}sp", fontWeight = FontWeight.Bold, color = AccentYellow, fontSize = 13.sp)
                                }
                                Slider(
                                    value = selectedFontSize.toFloat(),
                                    onValueChange = { selectedFontSize = it.toInt() },
                                    valueRange = 22f..72f,
                                    colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apply Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val updated = subtitle.copy(
                                fontName = selectedFont,
                                fontSize = selectedFontSize,
                                textColorHex = selectedTextColor,
                                outlineColorHex = selectedOutlineColor,
                                animationPreset = selectedAnimation,
                                textEffect = selectedEffect
                            )
                            onApplyToItem(updated)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply to Caption", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
