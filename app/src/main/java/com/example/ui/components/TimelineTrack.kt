package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterEffectType
import com.example.model.TransitionType
import com.example.model.VideoProject
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
import com.example.util.DualJsonParser
import java.util.Locale

@Composable
fun TimelineTrack(
    modifier: Modifier = Modifier,
    project: VideoProject,
    currentTimeSeconds: Double,
    selectedSceneIndex: Int?,
    selectedSubtitleId: String?,
    selectedAudioId: String? = null,
    selectedOverlayId: String? = null,
    onSeek: (Double) -> Unit,
    onSelectScene: (Int) -> Unit,
    onSelectSubtitle: (String) -> Unit,
    onSelectAudio: (String) -> Unit = {},
    onSelectOverlay: (String) -> Unit = {},
    onUpdateSceneDuration: (String, Double) -> Unit,
    onUpdateSceneTransition: (String, TransitionType) -> Unit,
    onUpdateSceneFilter: (String, FilterEffectType) -> Unit,
    onRemoveScene: (String) -> Unit,
    onRemoveSubtitle: (String) -> Unit,
    onRemoveAudio: (String) -> Unit = {},
    onNudgeAudio: (String, Double) -> Unit = { _, _ -> },
    onToggleAudioMute: (String) -> Unit = {},
    onDuplicateAudio: (String) -> Unit = {},
    onRemoveOverlay: (String) -> Unit = {},
    onNudgeOverlay: (String, Double) -> Unit = { _, _ -> },
    onAddMediaClick: () -> Unit,
    onAddSubtitleClick: () -> Unit,
    onAddAudioClick: () -> Unit = {},
    onAddOverlayClick: () -> Unit = {},
    zoomScale: Float = 1.0f
) {
    val totalDuration = project.totalDurationSeconds
    val videoScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(vertical = 4.dp)
            .testTag("timeline_track_container")
    ) {
        // 1. Timecode & Multi-Track Scrub Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = DualJsonParser.formatSecondsToTimecode(currentTimeSeconds),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentPurple
                )
                Text(
                    text = " / ${DualJsonParser.formatSecondsToTimecode(totalDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Quick Track Add Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // + Media
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentPurple.copy(alpha = 0.2f))
                        .clickable { onAddMediaClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_media_timeline_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Video", fontSize = 11.sp, color = AccentPurple, fontWeight = FontWeight.Bold)
                    }
                }

                // + Audio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .clickable { onAddAudioClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_audio_timeline_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Audio", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }

                // + Caption
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentCyan.copy(alpha = 0.2f))
                        .clickable { onAddSubtitleClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_caption_timeline_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Subtitles, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Text", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                }

                // + Overlay PIP
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentYellow.copy(alpha = 0.2f))
                        .clickable { onAddOverlayClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_overlay_timeline_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ PIP", fontSize = 11.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Standard Red Playhead & Multi-Track Ruler Bar
        // Simple direct horizontal drag logic without hitbox multiplier
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurfaceElevated)
                .pointerInput(totalDuration) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val rulerW = size.width.toFloat()
                        if (rulerW > 0) {
                            val newTime = (change.position.x / rulerW) * totalDuration
                            onSeek(newTime.toDouble().coerceIn(0.0, totalDuration))
                        }
                    }
                }
                .testTag("timeline_red_playhead_ruler")
        ) {
            val rulerWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(10f)

            // Ruler tick lines Canvas
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val numTicks = 20
                for (i in 0..numTicks) {
                    val x = (i.toFloat() / numTicks) * size.width
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = androidx.compose.ui.geometry.Offset(x, size.height - 8f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = 1.5f
                    )
                }
            }

            // Simple Standard Vertical Red Playhead Line
            val playheadRatio = (currentTimeSeconds / totalDuration.coerceAtLeast(0.1)).toFloat().coerceIn(0f, 1f)
            val playheadPx = playheadRatio * rulerWidthPx
            val density = androidx.compose.ui.platform.LocalDensity.current
            val playheadOffsetDp = with(density) { (playheadPx - 5f).coerceIn(0f, rulerWidthPx).toDp() }

            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp)
                    .width(10.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Top marker handle
                Box(
                    modifier = Modifier
                        .size(8.dp, 6.dp)
                        .background(Color(0xFFFF2A4B), shape = RoundedCornerShape(2.dp))
                )
                // Clean standard vertical red line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFFF2A4B))
                        .testTag("red_playhead_line")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // TRACK 1: Video Scenes & Transitions (Main Track)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRACK 1: MAIN VIDEO SCENES (${project.images.size})",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontSize = 10.sp),
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(videoScrollState)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            project.images.forEachIndexed { index, scene ->
                val isSelected = selectedSceneIndex == index
                val itemWidth = (scene.durationSeconds * 40.0 * zoomScale).dp.coerceIn(12.dp, 800.dp)

                // Scene Block
                Card(
                    modifier = Modifier
                        .width(itemWidth)
                        .height(72.dp)
                        .clickable { onSelectScene(index) }
                        .testTag("scene_block_$index"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentPurple.copy(alpha = 0.35f) else DarkSurfaceVariant
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AccentPurple) else null
                ) {
                    Column(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scene ${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = TextPrimary
                            )
                            if (isSelected && project.images.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(AccentRed.copy(alpha = 0.25f))
                                        .clickable { onRemoveScene(scene.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(11.dp))
                                }
                            }
                        }

                        // Duration Tweaker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1fs", scene.durationSeconds),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = AccentYellow,
                                fontWeight = FontWeight.Bold
                            )

                            if (isSelected) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable {
                                                if (scene.durationSeconds > 0.5) {
                                                    onUpdateSceneDuration(scene.id, scene.durationSeconds - 0.5)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable {
                                                onUpdateSceneDuration(scene.id, scene.durationSeconds + 0.5)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Filter & Motion Badge
                        Text(
                            text = "${scene.filterEffect.label} • ${scene.motionAnimation.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // CapCut Transition Swap Pill
                if (index < project.images.size - 1) {
                    var transitionMenuOpen by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.2f))
                            .border(1.dp, AccentCyan.copy(alpha = 0.6f), CircleShape)
                            .clickable { transitionMenuOpen = true }
                            .testTag("transition_pill_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transition",
                            tint = AccentCyan,
                            modifier = Modifier.size(14.dp)
                        )

                        DropdownMenu(
                            expanded = transitionMenuOpen,
                            onDismissRequest = { transitionMenuOpen = false }
                        ) {
                            Text(
                                text = "Select CapCut Transition",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            TransitionType.entries.forEach { trans ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = trans.label,
                                            fontWeight = if (scene.transition == trans) FontWeight.Bold else FontWeight.Normal,
                                            color = if (scene.transition == trans) AccentPurple else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        onUpdateSceneTransition(scene.id, trans)
                                        transitionMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TRACK 2: Overlay / PIP Images (Multi-Layer Images)
        if (project.overlays.isNotEmpty()) {
            Text(
                text = "TRACK 2: OVERLAY / PIP LAYERS (${project.overlays.size})",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontSize = 10.sp),
                color = AccentYellow,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                project.overlays.forEach { ov ->
                    val isSelected = selectedOverlayId == ov.id
                    Card(
                        modifier = Modifier
                            .clickable { onSelectOverlay(ov.id) }
                            .testTag("overlay_track_item_${ov.id}"),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AccentYellow.copy(alpha = 0.3f) else DarkSurfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "PIP: ${String.format(Locale.US, "%.1fs - %.1fs", ov.startSeconds, ov.startSeconds + ov.durationSeconds)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Scale: ${(ov.scale * 100).toInt()}% • Pos: (${(ov.posX * 100).toInt()}%, ${(ov.posY * 100).toInt()}%)",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    // Move Left
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable { onNudgeOverlay(ov.id, -0.5) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("<", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    // Move Right
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable { onNudgeOverlay(ov.id, 0.5) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(">", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    // Delete
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AccentRed.copy(alpha = 0.25f))
                                            .clickable { onRemoveOverlay(ov.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(11.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // TRACK 3: Subtitles / Text Track (Multi-Layer Captions)
        Text(
            text = "TRACK 3: SUBTITLE / TEXT LAYERS (${project.subtitles.size})",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontSize = 10.sp),
            color = AccentCyan,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            project.subtitles.forEach { sub ->
                val isSelected = selectedSubtitleId == sub.id
                Card(
                    modifier = Modifier
                        .clickable { onSelectSubtitle(sub.id) }
                        .testTag("sub_item_${sub.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentCyan.copy(alpha = 0.3f) else DarkSurfaceVariant
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sub.text.ifEmpty { "(empty caption)" },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = String.format(Locale.US, "%.1fs - %.1fs", sub.startSeconds, sub.endSeconds),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                    color = AccentYellow
                                )
                                Text(
                                    text = sub.animationPreset.label,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                    color = TextMuted
                                )
                            }
                        }
                        if (isSelected && project.subtitles.size > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(AccentRed.copy(alpha = 0.25f))
                                    .clickable { onRemoveSubtitle(sub.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TRACK 4: Audio Track Studio (Multi-Audio Tracks with Drag / Move / Nudge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRACK 4: AUDIO / BGM & SFX (${project.audioTracks.size})",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontSize = 10.sp),
                color = Color(0xFF10B981)
            )
            Text(
                text = "Tap to Select & Move / Drag Position",
                fontSize = 9.sp,
                color = TextMuted
            )
        }

        if (project.audioTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { onAddAudioClick() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Audio Track (BGM, Sound Effects, Music)", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                project.audioTracks.forEach { track ->
                    val isSelected = selectedAudioId == track.id
                    Card(
                        modifier = Modifier
                            .clickable { onSelectAudio(track.id) }
                            .testTag("audio_track_item_${track.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF10B981).copy(alpha = 0.28f) else DarkSurfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (track.isBgm) Icons.Default.MusicNote else Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = if (track.isBgm) Color(0xFF10B981) else AccentCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = track.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Mute Toggle
                                IconButton(
                                    onClick = { onToggleAudioMute(track.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        if (track.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Mute",
                                        tint = if (track.isMuted) AccentRed else Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Timing & Volume
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1fs - %.1fs", track.startSeconds, track.startSeconds + track.durationSeconds),
                                    fontSize = 9.sp,
                                    color = AccentYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vol: ${(track.volume * 100).toInt()}%",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }

                            // Interactive Drag & Nudge Controls (CapCut Style)
                            if (isSelected) {
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Move -1.0s
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable { onNudgeAudio(track.id, -1.0) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("<< -1s", fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }

                                    // Move -0.2s
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable { onNudgeAudio(track.id, -0.2) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("<", fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }

                                    // Move +0.2s
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable { onNudgeAudio(track.id, 0.2) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(">", fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }

                                    // Move +1.0s
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceElevated)
                                            .clickable { onNudgeAudio(track.id, 1.0) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+1s >>", fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }

                                    // Duplicate
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AccentCyan.copy(alpha = 0.2f))
                                            .clickable { onDuplicateAudio(track.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = AccentCyan, modifier = Modifier.size(11.dp))
                                    }

                                    // Delete
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AccentRed.copy(alpha = 0.25f))
                                            .clickable { onRemoveAudio(track.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(11.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
