package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExportConfig
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.model.FilterEffectType
import com.example.model.ProjectAspectRatio
import com.example.model.TextSyncItem
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.model.FramingMode
import com.example.model.ImageMotionAnimation
import com.example.model.TransitionType
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.ui.unit.IntOffset
import com.example.data.AssetCategory
import com.example.ui.components.AudioPickerDialog
import com.example.ui.components.AudioVolumeDialog
import com.example.ui.components.CloudAssetStoreBottomSheet
import com.example.ui.components.ExportProgressDialog
import com.example.ui.components.JsonScriptAutomationDialog
import com.example.ui.components.OverlayStudioDialog
import com.example.ui.components.SceneEffectsStudioDialog
import com.example.ui.components.SubtitleStyleDialog
import com.example.ui.components.TimelineTrack
import com.example.ui.components.TransitionSelectorDialog
import com.example.util.DualJsonParser
import java.util.Locale
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ImageResolver
import com.example.video.VideoRenderEffects
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onOpenDualJson: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val project by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeSeconds by viewModel.currentTimeSeconds.collectAsState()
    val selectedSceneIndex by viewModel.selectedSceneIndex.collectAsState()
    val selectedSubtitleId by viewModel.selectedSubtitleId.collectAsState()
    val selectedAudioId by viewModel.selectedAudioId.collectAsState()
    val selectedOverlayId by viewModel.selectedOverlayId.collectAsState()
    val renderProgress by viewModel.renderProgress.collectAsState()

    var showStyleDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showEditSubtitleDialog by remember { mutableStateOf(false) }
    var showCustomExportDialog by remember { mutableStateOf(false) }
    var showMediaImportDialog by remember { mutableStateOf(false) }
    var showAudioPickerDialog by remember { mutableStateOf(false) }
    var showAudioVolumeDialog by remember { mutableStateOf(false) }
    var showOverlayStudioDialog by remember { mutableStateOf(false) }
    var showTransitionDialog by remember { mutableStateOf(false) }
    val zoomScale by viewModel.zoomScale.collectAsState()
    var showJsonAutomationDialog by remember { mutableStateOf(false) }
    var showAssetStoreSheet by remember { mutableStateOf(false) }

    // Audio file picker launcher (MP3, WAV, M4A, AAC)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                viewModel.addAudioFromUri(uri, isBgm = true)
            }
            Toast.makeText(context, "${uris.size} audio files added", Toast.LENGTH_SHORT).show()
        }
    }

    // Overlay PIP picker launcher
    val overlayPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                viewModel.addOverlay(uri)
            }
            Toast.makeText(context, "${uris.size} overlay PIP added", Toast.LENGTH_SHORT).show()
        }
    }

    // 1. Multi-photo picker launcher (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addMediaUris(uris)
            Toast.makeText(context, "${uris.size} photos added from gallery", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Storage & Files picker launcher (Internal storage, SD Card, Downloads)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addMediaUris(uris)
            Toast.makeText(context, "${uris.size} files added from storage", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Whole Folder picker launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val foundUris = mutableListOf<Uri>()
                try {
                    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                        treeUri,
                        DocumentsContract.getTreeDocumentId(treeUri)
                    )
                    context.contentResolver.query(
                        childrenUri,
                        arrayOf(
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_MIME_TYPE
                        ),
                        null, null, null
                    )?.use { cursor ->
                        val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                        while (cursor.moveToNext()) {
                            val mime = cursor.getString(mimeIndex)
                            if (mime != null && (mime.startsWith("image/") || mime.contains("jpg") || mime.contains("png") || mime.contains("webp"))) {
                                val docId = cursor.getString(idIndex)
                                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                                foundUris.add(docUri)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback
                }
                withContext(Dispatchers.Main) {
                    if (foundUris.isNotEmpty()) {
                        viewModel.addMediaUris(foundUris)
                        Toast.makeText(context, "${foundUris.size} images imported from folder!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No images found in selected folder", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
            Text("No active project", color = TextSecondary)
        }
        return
    }

    val activeProject = project!!
    val currentSub = activeProject.subtitles.find { it.id == selectedSubtitleId }
        ?: activeProject.subtitles.find { currentTimeSeconds >= it.startSeconds && currentTimeSeconds <= it.endSeconds }
        ?: activeProject.subtitles.firstOrNull()

    val currentOverlay = activeProject.overlays.find { it.id == selectedOverlayId }
        ?: activeProject.overlays.find { currentTimeSeconds >= it.startSeconds && currentTimeSeconds < it.startSeconds + it.durationSeconds }
        ?: activeProject.overlays.firstOrNull()

    val currentAudio = activeProject.audioTracks.find { it.id == selectedAudioId }
        ?: activeProject.audioTracks.firstOrNull()

    // Pre-load scene bitmaps for preview drawing
    val projectDir = remember(activeProject.metadata.id) {
        File(File(context.filesDir, "projects"), activeProject.metadata.id)
    }

    var previewBitmaps by remember(activeProject.images) {
        mutableStateOf<List<Bitmap>>(emptyList())
    }

    var overlayBitmaps by remember(activeProject.overlays) {
        mutableStateOf<Map<String, Bitmap>>(emptyMap())
    }

    LaunchedEffect(activeProject.overlays) {
        val map = mutableMapOf<String, Bitmap>()
        for (ov in activeProject.overlays) {
            if (!map.containsKey(ov.imagePath)) {
                try {
                    val bmp = ImageResolver.loadBitmap(
                        context = context,
                        projectDir = projectDir,
                        pathOrUri = ov.imagePath,
                        targetWidth = 450,
                        targetHeight = 450,
                        framingMode = FramingMode.CROP_TO_FILL
                    )
                    if (bmp != null) map[ov.imagePath] = bmp
                } catch (_: Exception) {}
            }
        }
        overlayBitmaps = map
    }

    LaunchedEffect(activeProject.images, activeProject.metadata.aspectRatio) {
        val loaded = activeProject.images.map { img ->
            ImageResolver.loadBitmap(
                context = context,
                projectDir = projectDir,
                pathOrUri = img.imagePath,
                targetWidth = 720,
                targetHeight = 1280,
                framingMode = img.framingMode
            )
        }
        previewBitmaps = loaded
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeProject.metadata.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${activeProject.metadata.aspectRatio.label} (${activeProject.metadata.aspectRatio.width}x${activeProject.metadata.aspectRatio.height})",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AccentCyan
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    // Aspect Ratio Switcher
                    IconButton(
                        onClick = {
                            val nextRatio = when (activeProject.metadata.aspectRatio) {
                                ProjectAspectRatio.SHORTS_9_16 -> ProjectAspectRatio.YOUTUBE_16_9
                                ProjectAspectRatio.YOUTUBE_16_9 -> ProjectAspectRatio.SQUARE_1_1
                                ProjectAspectRatio.SQUARE_1_1 -> ProjectAspectRatio.PORTRAIT_4_5
                                ProjectAspectRatio.PORTRAIT_4_5 -> ProjectAspectRatio.SHORTS_9_16
                            }
                            viewModel.updateAspectRatio(nextRatio)
                        },
                        modifier = Modifier.testTag("toggle_aspect_ratio_button")
                    ) {
                        Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = TextSecondary)
                    }

                    // Import Raw JSON Script Automation
                    IconButton(
                        onClick = { showJsonAutomationDialog = true },
                        modifier = Modifier.testTag("import_json_script_appbar_button")
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = "Import JSON Script", tint = AccentYellow)
                    }

                    // Dual JSON Code View
                    IconButton(
                        onClick = onOpenDualJson,
                        modifier = Modifier.testTag("open_dual_json_button")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Dual-JSON", tint = AccentCyan)
                    }

                    // Export MP4 Button
                    Button(
                        onClick = { showCustomExportDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(38.dp)
                            .testTag("export_mp4_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export MP4", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Studio Live Compact Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .background(Color.Black)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeProject.images.isEmpty()) {
                        // Clean Empty Media State (CapCut style)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "No Media Added Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Import photos/videos to start editing",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        filePickerLauncher.launch(arrayOf("image/*", "video/*"))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Storage / Files", fontSize = 11.sp, color = TextPrimary)
                                }
                                OutlinedButton(
                                    onClick = {
                                        folderPickerLauncher.launch(null)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Folder", fontSize = 11.sp, color = AccentCyan)
                                }
                            }
                        }
                    } else {
                        val ratio = activeProject.metadata.aspectRatio.aspectRatio
                        val isWider = if (maxHeight.value > 0f) (maxWidth.value / maxHeight.value) > ratio else false
                        val previewBoxModifier = if (isWider) {
                            Modifier.fillMaxHeight().aspectRatio(ratio)
                        } else {
                            Modifier.fillMaxWidth().aspectRatio(ratio)
                        }

                        // Interactive Subtitle & Overlay Dragging Container
                        Box(
                            modifier = previewBoxModifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .pointerInput(currentSub?.id, currentOverlay?.id, selectedOverlayId) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (size.width > 0 && size.height > 0) {
                                        val deltaX = dragAmount.x / size.width
                                        val deltaY = dragAmount.y / size.height
                                        if (selectedOverlayId != null && currentOverlay != null) {
                                            viewModel.updateOverlayPosition(
                                                currentOverlay.id,
                                                currentOverlay.posX + deltaX,
                                                currentOverlay.posY + deltaY,
                                                currentOverlay.scale
                                            )
                                        } else if (currentSub != null) {
                                            viewModel.updateSubtitlePosition(
                                                currentSub.id,
                                                currentSub.posX + deltaX,
                                                currentSub.posY + deltaY
                                            )
                                        }
                                    }
                                }
                            }
                            .clickable { viewModel.togglePlay() }
                            .testTag("live_studio_preview")
                    ) {
                        // Jetpack Compose Canvas executing real-time VideoRenderEffects
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width.toInt()
                            val h = size.height.toInt()
                            if (w > 0 && h > 0) {
                                // 1. Draw Background Scenes & Transitions
                                VideoRenderEffects.renderScenes(
                                    canvas = drawContext.canvas.nativeCanvas,
                                    images = activeProject.images,
                                    bitmaps = previewBitmaps,
                                    currentTimeSeconds = currentTimeSeconds,
                                    width = w,
                                    height = h
                                )

                                // 2. Draw Overlays / Picture-In-Picture Layers
                                if (activeProject.overlays.isNotEmpty() && overlayBitmaps.isNotEmpty()) {
                                    VideoRenderEffects.renderOverlays(
                                        canvas = drawContext.canvas.nativeCanvas,
                                        overlays = activeProject.overlays,
                                        overlayBitmaps = overlayBitmaps,
                                        currentTimeSeconds = currentTimeSeconds,
                                        width = w,
                                        height = h
                                    )
                                }

                                // 3. Draw Animated Subtitles
                                VideoRenderEffects.renderSubtitles(
                                    canvas = drawContext.canvas.nativeCanvas,
                                    subtitles = activeProject.subtitles,
                                    currentTimeSeconds = currentTimeSeconds,
                                    width = w,
                                    height = h,
                                    scaleFactor = w.toFloat() / 1080f
                                )
                            }
                        }

                        // Play/Pause overlay indicator (shows when paused)
                        if (!isPlaying) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x88000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Draggable Text Overlays (direct on canvas positioning)
                        activeProject.subtitles.filter {
                            currentTimeSeconds >= it.startSeconds && currentTimeSeconds <= it.endSeconds
                        }.forEach { subItem ->
                            val isSelected = selectedSubtitleId == subItem.id
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset {
                                        val parentW = this@BoxWithConstraints.maxWidth.toPx()
                                        val parentH = this@BoxWithConstraints.maxHeight.toPx()
                                        IntOffset(
                                            x = ((subItem.posX * parentW) - 60f).toInt().coerceIn(0, maxOf(0, (parentW - 120).toInt())),
                                            y = ((subItem.posY * parentH) - 25f).toInt().coerceIn(0, maxOf(0, (parentH - 50).toInt()))
                                        )
                                    }
                                    .pointerInput(subItem.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val parentW = this@BoxWithConstraints.maxWidth.toPx()
                                            val parentH = this@BoxWithConstraints.maxHeight.toPx()
                                            if (parentW > 0 && parentH > 0) {
                                                val deltaX = dragAmount.x / parentW
                                                val deltaY = dragAmount.y / parentH
                                                viewModel.updateSubtitlePosition(
                                                    subItem.id,
                                                    subItem.posX + deltaX,
                                                    subItem.posY + deltaY
                                                )
                                            }
                                        }
                                    }
                                    .clickable { viewModel.selectSubtitle(subItem.id) }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) AccentCyan else Color(0x6600F0FF),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(Color(0x33000000), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("draggable_text_${subItem.id}")
                            ) {
                                // Specific video text style: Solid White fill with Black Stroke / Border
                                Box(contentAlignment = Alignment.Center) {
                                    // Black outline stroke
                                    Text(
                                        text = subItem.text.ifEmpty { "Text" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        style = androidx.compose.ui.text.TextStyle(
                                            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                                                miter = 10f,
                                                width = 3.5f,
                                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                                            ),
                                            color = Color.Black
                                        )
                                    )
                                    // Solid White fill
                                    Text(
                                        text = subItem.text.ifEmpty { "Text" },
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Touch to drag hint badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x77000000))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (selectedOverlayId != null) "Drag PIP to position" else "Drag text to position",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = if (selectedOverlayId != null) AccentYellow else TextMuted
                            )
                        }
                    }
                }
            }
        }

            // Global Scrubber: Full-width Slider directly below Video Preview Canvas (and above timeline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DualJsonParser.formatSecondsToTimecode(currentTimeSeconds),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = AccentCyan
                )
                Slider(
                    value = currentTimeSeconds.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toDouble()) },
                    valueRange = 0f..activeProject.totalDurationSeconds.toFloat().coerceAtLeast(1f),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("global_scrubber_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPurple,
                        activeTrackColor = AccentPurple,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
                Text(
                    text = DualJsonParser.formatSecondsToTimecode(activeProject.totalDurationSeconds),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }

            // 2. Studio Quick Action Bar (CapCut Style)
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Button
                    IconButton(
                        onClick = { viewModel.togglePlay() },
                        modifier = Modifier.size(36.dp).testTag("play_pause_button")
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = AccentPurple,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Zoom In/Out Slider (Middle Toolbar)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("timeline_zoom_toolbar_group")
                    ) {
                        Icon(
                            Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.setZoom((zoomScale - 0.2f).coerceAtLeast(0.1f)) }
                        )
                        Slider(
                            value = zoomScale,
                            onValueChange = { viewModel.setZoom(it) },
                            valueRange = 0.1f..3.0f,
                            modifier = Modifier
                                .width(90.dp)
                                .testTag("timeline_zoom_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan,
                                inactiveTrackColor = Color(0x22FFFFFF)
                            )
                        )
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "Zoom In",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.setZoom((zoomScale + 0.2f).coerceAtMost(3.0f)) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1fx", zoomScale),
                            fontSize = 10.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Cloud Asset Store Button (50+ Filters, FX, Transitions, Animations)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentCyan.copy(alpha = 0.2f))
                            .clickable { showAssetStoreSheet = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("open_cloud_asset_store_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterDrama, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cloud Store (50+)", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 1. Subtitle & Text Studio (Templates, Animations, Fonts, 3D Effects, Colors)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentPurple.copy(alpha = 0.2f))
                            .clickable { showStyleDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("open_styles_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Text Studio (100+)", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Audio Studio (Music, Sound Effects, Move/Drag, Volume)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .clickable {
                                if (currentAudio != null) {
                                    showAudioVolumeDialog = true
                                } else {
                                    showAudioPickerDialog = true
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("audio_studio_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audio Studio", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }

                    // 3. Overlays / Picture-In-Picture (Multi-Layer Images)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentYellow.copy(alpha = 0.2f))
                            .clickable { showOverlayStudioDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("overlay_studio_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Overlay / PIP", fontSize = 11.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 4. Filters & Ken Burns Motion (60+ Filters, Camera Motion)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showFilterDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("scene_filter_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filters & Motion (60+)", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    // 5. Transitions Library (60+ CapCut Transitions)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showTransitionDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("transitions_library_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Transitions (60+)", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    // 6. Add Media Button (Gallery / Storage / Folder)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentCyan.copy(alpha = 0.18f))
                            .clickable { showMediaImportDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("quick_add_media_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Media", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 7. Edit Caption Text
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showEditSubtitleDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("edit_caption_text_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Text", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // 3. Multi-Track Timeline Component
            TimelineTrack(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                project = activeProject,
                currentTimeSeconds = currentTimeSeconds,
                selectedSceneIndex = selectedSceneIndex,
                selectedSubtitleId = selectedSubtitleId,
                selectedAudioId = selectedAudioId,
                selectedOverlayId = selectedOverlayId,
                onSeek = { viewModel.seekTo(it) },
                onSelectScene = { viewModel.selectScene(it) },
                onSelectSubtitle = { viewModel.selectSubtitle(it) },
                onSelectAudio = {
                    viewModel.selectAudioTrack(it)
                    showAudioVolumeDialog = true
                },
                onSelectOverlay = {
                    viewModel.selectOverlay(it)
                    showOverlayStudioDialog = true
                },
                onUpdateSceneDuration = { id, dur -> viewModel.updateSceneDuration(id, dur) },
                onUpdateSceneTransition = { id, trans -> viewModel.updateSceneTransition(id, trans) },
                onUpdateSceneFilter = { id, filter -> viewModel.updateSceneFilter(id, filter) },
                onRemoveScene = { viewModel.removeScene(it) },
                onRemoveSubtitle = { viewModel.removeSubtitle(it) },
                onRemoveAudio = { viewModel.removeAudioTrack(it) },
                onNudgeAudio = { id, delta -> viewModel.nudgeAudioStart(id, delta) },
                onToggleAudioMute = { viewModel.toggleAudioMute(it) },
                onDuplicateAudio = { viewModel.duplicateAudioTrack(it) },
                onRemoveOverlay = { viewModel.removeOverlay(it) },
                onNudgeOverlay = { id, delta -> viewModel.nudgeOverlayStart(id, delta) },
                onAddMediaClick = {
                    showMediaImportDialog = true
                },
                onAddSubtitleClick = {
                    viewModel.addSubtitle("NEW VIRAL HOOK", currentTimeSeconds, currentTimeSeconds + 2.5)
                },
                onAddAudioClick = {
                    showAudioPickerDialog = true
                },
                onAddOverlayClick = {
                    overlayPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                zoomScale = zoomScale
            )
        }
    }

    // Subtitle Style & Animation Dialog
    if (showStyleDialog && currentSub != null) {
        SubtitleStyleDialog(
            subtitle = currentSub,
            onDismiss = { showStyleDialog = false },
            onApplyToItem = { updated ->
                viewModel.mutateSubtitle(updated)
            },
            onApplyPresetToAll = { preset ->
                viewModel.applyPresetToAllSubtitles(preset)
            }
        )
    }

    // Edit Subtitle Text Dialog
    if (showEditSubtitleDialog && currentSub != null) {
        EditSubtitleTextDialog(
            subtitle = currentSub,
            onDismiss = { showEditSubtitleDialog = false },
            onSave = { newText ->
                viewModel.updateSubtitleText(currentSub.id, newText)
                showEditSubtitleDialog = false
            }
        )
    }

    // Audio Picker Dialog (BGM / SFX library + Storage import)
    if (showAudioPickerDialog) {
        AudioPickerDialog(
            onDismiss = { showAudioPickerDialog = false },
            onPickFromStorage = {
                showAudioPickerDialog = false
                audioPickerLauncher.launch(arrayOf("audio/*"))
            },
            onSelectPreset = { preset ->
                viewModel.addAudioTrack(preset.id, preset.title, preset.isBgm)
                showAudioPickerDialog = false
            }
        )
    }

    // Audio Volume & Move / Trim Inspector Dialog
    if (showAudioVolumeDialog && currentAudio != null) {
        AudioVolumeDialog(
            audioTrack = currentAudio,
            maxTimelineDuration = activeProject.totalDurationSeconds,
            onDismiss = { showAudioVolumeDialog = false },
            onUpdateVolume = { vol -> viewModel.updateAudioVolume(currentAudio.id, vol) },
            onUpdateTiming = { start, dur -> viewModel.updateAudioTiming(currentAudio.id, start, dur) },
            onNudge = { delta -> viewModel.nudgeAudioStart(currentAudio.id, delta) },
            onToggleMute = { viewModel.toggleAudioMute(currentAudio.id) },
            onDuplicate = {
                viewModel.duplicateAudioTrack(currentAudio.id)
                showAudioVolumeDialog = false
            },
            onDelete = {
                viewModel.removeAudioTrack(currentAudio.id)
                showAudioVolumeDialog = false
            }
        )
    }

    // Overlay / PIP Studio Dialog
    if (showOverlayStudioDialog) {
        OverlayStudioDialog(
            overlay = currentOverlay,
            onDismiss = { showOverlayStudioDialog = false },
            onAddNewOverlay = {
                overlayPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onUpdateProperties = { px, py, sc, rot, op ->
                if (currentOverlay != null) {
                    viewModel.updateOverlayProperties(currentOverlay.id, px, py, sc, rot, op)
                }
            },
            onUpdateTiming = { start, dur ->
                if (currentOverlay != null) {
                    viewModel.updateOverlayTiming(currentOverlay.id, start, dur)
                }
            },
            onNudge = { delta ->
                if (currentOverlay != null) {
                    viewModel.nudgeOverlayStart(currentOverlay.id, delta)
                }
            },
            onDuplicate = {
                if (currentOverlay != null) {
                    viewModel.duplicateOverlay(currentOverlay.id)
                }
            },
            onDelete = {
                if (currentOverlay != null) {
                    viewModel.removeOverlay(currentOverlay.id)
                    showOverlayStudioDialog = false
                }
            }
        )
    }

    // Scene Filter & Ken Burns Motion Dialog
    if (showFilterDialog) {
        val scene = selectedSceneIndex?.let { activeProject.images.getOrNull(it) } ?: activeProject.images.firstOrNull()
        if (scene != null) {
            SceneEffectsStudioDialog(
                scene = scene,
                onDismiss = { showFilterDialog = false },
                onSelectFilter = { filter ->
                    viewModel.updateSceneFilter(scene.id, filter)
                },
                onSelectMotion = { motion ->
                    viewModel.updateSceneMotion(scene.id, motion)
                }
            )
        }
    }

    // Transitions Library Dialog
    if (showTransitionDialog) {
        val scene = selectedSceneIndex?.let { activeProject.images.getOrNull(it) } ?: activeProject.images.firstOrNull()
        if (scene != null) {
            TransitionSelectorDialog(
                currentTransition = scene.transition,
                onDismiss = { showTransitionDialog = false },
                onSelectTransition = { trans ->
                    viewModel.updateSceneTransition(scene.id, trans)
                    showTransitionDialog = false
                }
            )
        }
    }

    // Custom Export Studio Dialog (720p, 1080p, 2K, 4K, FPS, Bitrate)
    if (showCustomExportDialog) {
        CustomExportStudioDialog(
            aspectRatio = activeProject.metadata.aspectRatio,
            onDismiss = { showCustomExportDialog = false },
            onStartExport = { config ->
                showCustomExportDialog = false
                viewModel.startExport(config)
            }
        )
    }

    // Media Import Dialog (Gallery, Storage/Files, Whole Folder)
    if (showMediaImportDialog) {
        MediaImportDialog(
            onDismiss = { showMediaImportDialog = false },
            onPickGallery = {
                showMediaImportDialog = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickFiles = {
                showMediaImportDialog = false
                filePickerLauncher.launch(arrayOf("image/*"))
            },
            onPickFolder = {
                showMediaImportDialog = false
                folderPickerLauncher.launch(null)
            }
        )
    }

    // Hardware Video Export Progress Dialog
    ExportProgressDialog(
        renderProgress = renderProgress,
        onDismiss = { viewModel.resetExportState() },
        onOpenGallery = { viewModel.openExportedVideo() },
        onShareVideo = { viewModel.shareExportedVideo() }
    )

    // JSON Automation Script Dialog (Import & Execute multi-layer script)
    if (showJsonAutomationDialog) {
        val currentScript = DualJsonParser.serializeTimelineItemsJson(activeProject.toProjectModel().items)
        JsonScriptAutomationDialog(
            currentScriptJson = currentScript,
            onDismiss = { showJsonAutomationDialog = false },
            onExecuteScript = { rawJson ->
                val res = viewModel.importJsonScript(rawJson)
                res.onSuccess { count ->
                    Toast.makeText(context, "Successfully loaded $count tracks from JSON script!", Toast.LENGTH_SHORT).show()
                    showJsonAutomationDialog = false
                }.onFailure { err ->
                    Toast.makeText(context, "Script parsing error: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Cloud Asset Store BottomSheet (CapCut Style: 50+ Filters, FX, Transitions, Animations)
    if (showAssetStoreSheet) {
        CloudAssetStoreBottomSheet(
            onDismiss = { showAssetStoreSheet = false },
            onApplyAsset = { asset ->
                when (asset.category) {
                    AssetCategory.ANIMATION -> {
                        if (currentSub != null) {
                            viewModel.applyAnimation(currentSub.id, asset.effectTag, "FADE_IN")
                            Toast.makeText(context, "Applied ${asset.name} animation to text!", Toast.LENGTH_SHORT).show()
                        } else if (activeProject.images.isNotEmpty()) {
                            val scene = activeProject.images.getOrNull(selectedSceneIndex ?: 0)
                            if (scene != null) {
                                val motion = try {
                                    com.example.model.ImageMotionAnimation.valueOf(asset.effectTag)
                                } catch (_: Exception) {
                                    com.example.model.ImageMotionAnimation.KEN_BURNS_ZOOM_IN
                                }
                                viewModel.updateSceneMotion(scene.id, motion)
                                Toast.makeText(context, "Applied ${asset.name} motion to scene!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    AssetCategory.EFFECT -> {
                        if (currentSub != null) {
                            viewModel.applyEffect(currentSub.id, asset.effectTag)
                            Toast.makeText(context, "Applied ${asset.name} effect to text!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "${asset.name} applied!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    AssetCategory.FILTER -> {
                        val scene = activeProject.images.getOrNull(selectedSceneIndex ?: 0) ?: activeProject.images.firstOrNull()
                        if (scene != null) {
                            viewModel.applyFilter(scene.id, asset.effectTag)
                            Toast.makeText(context, "Applied ${asset.name} filter to scene!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    AssetCategory.TRANSITION -> {
                        val scene = activeProject.images.getOrNull(selectedSceneIndex ?: 0) ?: activeProject.images.firstOrNull()
                        if (scene != null) {
                            viewModel.applyTransition(scene.id, asset.effectTag)
                            Toast.makeText(context, "Applied ${asset.name} transition to scene!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}

// Helper extension on ViewModel
private fun EditorViewModel.mutateSubtitle(sub: TextSyncItem) {
    applySubtitlePreset(
        sub.id,
        com.example.model.AssPresetStyle(
            name = "Custom",
            description = "",
            fontName = sub.fontName,
            fontSize = sub.fontSize,
            textColorHex = sub.textColorHex,
            outlineColorHex = sub.outlineColorHex,
            animationPreset = sub.animationPreset,
            textEffect = sub.textEffect
        )
    )
}

@Composable
fun EditSubtitleTextDialog(
    subtitle: TextSyncItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(subtitle.text) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth().testTag("edit_subtitle_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit Caption Text",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().testTag("caption_text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkSurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = { onSave(text) },
                        modifier = Modifier.weight(1f).testTag("save_caption_text_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomExportStudioDialog(
    aspectRatio: ProjectAspectRatio,
    onDismiss: () -> Unit,
    onStartExport: (ExportConfig) -> Unit
) {
    var selectedRes by remember { mutableStateOf(ExportResolution.FHD_1080P) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }
    var customBitrate by remember { mutableFloatStateOf(8f) }

    LaunchedEffect(selectedRes) {
        customBitrate = (selectedRes.defaultBitrate / 1_000_000).toFloat()
    }

    val (estWidth, estHeight) = remember(selectedRes, aspectRatio) {
        val shortSide = selectedRes.shortSidePixels
        if (aspectRatio.width <= aspectRatio.height) {
            val w = shortSide
            val h = (shortSide / aspectRatio.aspectRatio).toInt()
            Pair((w / 2) * 2, (h / 2) * 2)
        } else {
            val h = shortSide
            val w = (shortSide * aspectRatio.aspectRatio).toInt()
            Pair((w / 2) * 2, (h / 2) * 2)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_export_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "কাস্টমাইজড এক্সপোর্ট স্টুডিও",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Samsung Exynos 10x হার্ডওয়্যার এক্সেলারেশন · MP4 Export",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Resolution Selection (720p, 1080p, 2K, 4K)
                Text(
                    text = "ভিডিও রেজোলিউশন (Resolution):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportResolution.entries.forEach { res ->
                        val isSelected = selectedRes == res
                        val shortLabel = when (res) {
                            ExportResolution.HD_720P -> "720p"
                            ExportResolution.FHD_1080P -> "1080p"
                            ExportResolution.QHD_2K -> "2K"
                            ExportResolution.UHD_4K -> "4K"
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRes = res },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AccentPurple else DarkSurfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = shortLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. FPS Selection (24, 30, 60)
                Text(
                    text = "ফ্রেম রেট (Frame Rate):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportFps.entries.forEach { fpsOption ->
                        val isSelected = selectedFps == fpsOption
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFps = fpsOption },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AccentCyan else DarkSurfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${fpsOption.fps} FPS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Bitrate Control Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বিটরেট (Bitrate Quality):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyan
                    )
                    Text(
                        text = "${customBitrate.toInt()} Mbps",
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = customBitrate,
                    onValueChange = { customBitrate = it },
                    valueRange = 4f..45f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentYellow,
                        activeTrackColor = AccentYellow,
                        inactiveTrackColor = DarkSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Output Summary Box
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📐 আউটপুট মাপ: ${estWidth} x ${estHeight} · ${aspectRatio.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "⚡ কোডেক: H.264 AVC High Profile · ${selectedFps.fps} fps · ${customBitrate.toInt()} Mbps",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = "📁 গ্যালারিতে সেভ: Movies/FacelessEditor/",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AccentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val config = ExportConfig(
                                resolution = selectedRes,
                                fps = selectedFps,
                                customBitrateMbps = customBitrate.toInt(),
                                useHardwareAcceleration = true
                            )
                            onStartExport(config)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("start_hardware_render_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Export", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MediaImportDialog(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onPickFiles: () -> Unit,
    onPickFolder: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth().testTag("media_import_dialog")
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "মিডিয়া ইম্পোর্ট অপশন (Add Media)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "যেখান থেকে ছবি যুক্ত করতে চান নির্বাচন করুন:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Gallery
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickGallery() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AccentPurple)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ফোন গ্যালারি (Gallery)", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("গ্যালারির ফটো ও অ্যালবাম থেকে নির্বাচন করুন", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Storage & Files
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickFiles() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = AccentCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ফাইল ম্যানেজার ও স্টোরেজ (Files)", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Internal Storage, Downloads, SD Card ব্রাউজ করুন", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Whole Folder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickFolder() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DriveFolderUpload, contentDescription = null, tint = AccentYellow)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("সম্পূর্ণ ফোল্ডার ইম্পোর্ট (Import Folder)", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("একটি ফোল্ডারের সব ছবি একসাথে যুক্ত করুন", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    }
}
