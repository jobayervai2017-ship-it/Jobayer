package com.example.model

enum class ProjectAspectRatio(
    val label: String,
    val width: Int,
    val height: Int,
    val description: String
) {
    SHORTS_9_16("9:16", 1080, 1920, "Shorts, Reels, TikTok"),
    YOUTUBE_16_9("16:9", 1920, 1080, "YouTube Landscape"),
    SQUARE_1_1("1:1", 1080, 1080, "Instagram Square"),
    PORTRAIT_4_5("4:5", 1080, 1350, "Instagram Portrait");

    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
}

enum class ExportResolution(
    val label: String,
    val shortSidePixels: Int,
    val scaleFactor: Float,
    val defaultBitrate: Int
) {
    HD_720P("720p HD (Fast Export)", 720, 0.6667f, 4_000_000),
    FHD_1080P("1080p FHD (Recommended)", 1080, 1.0f, 8_000_000),
    QHD_2K("2K QHD (1440p Ultra Clear)", 1440, 1.3333f, 16_000_000),
    UHD_4K("4K UHD (2160p Master)", 2160, 2.0f, 32_000_000)
}

enum class ExportFps(
    val fps: Int,
    val label: String
) {
    FPS_24(24, "24 FPS (Cinematic)"),
    FPS_30(30, "30 FPS (Standard)"),
    FPS_60(60, "60 FPS (Ultra Smooth)")
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.FHD_1080P,
    val fps: ExportFps = ExportFps.FPS_30,
    val customBitrateMbps: Int = 8,
    val useHardwareAcceleration: Boolean = true
) {
    val bitrate: Int
        get() = if (customBitrateMbps > 0) customBitrateMbps * 1_000_000 else resolution.defaultBitrate
}

data class AudioTrackItem(
    val id: String,
    val audioPath: String, // Can be local path, uri, or builtin id
    val title: String,
    val startSeconds: Double = 0.0,
    val durationSeconds: Double = 10.0,
    val audioOffsetSeconds: Double = 0.0,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isBgm: Boolean = true // true = Background Music, false = SFX / Voice
)

data class OverlayImageItem(
    val id: String,
    val imagePath: String,
    val startSeconds: Double = 0.0,
    val durationSeconds: Double = 3.0,
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val scale: Float = 0.45f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f
)

data class ProjectMetadata(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val aspectRatio: ProjectAspectRatio = ProjectAspectRatio.SHORTS_9_16
)

data class VideoProject(
    val metadata: ProjectMetadata,
    val images: List<ImageSyncItem> = emptyList(),
    val overlays: List<OverlayImageItem> = emptyList(),
    val subtitles: List<TextSyncItem> = emptyList(),
    val audioTracks: List<AudioTrackItem> = emptyList()
) {
    val totalDurationSeconds: Double
        get() {
            val imageSum = images.sumOf { it.durationSeconds }
            val subEnd = subtitles.maxOfOrNull { it.endSeconds } ?: 0.0
            val audioEnd = audioTracks.maxOfOrNull { it.startSeconds + it.durationSeconds } ?: 0.0
            val overlayEnd = overlays.maxOfOrNull { it.startSeconds + it.durationSeconds } ?: 0.0
            val total = maxOf(imageSum, subEnd, audioEnd, overlayEnd)
            return if (total > 0.0) total else 3.0
        }

    fun toProjectModel(): ProjectModel {
        val items = mutableListOf<TimelineItemModel>()
        var currentMs = 0L

        // Main media track (layer 0)
        images.forEachIndexed { idx, img ->
            val durMs = (img.durationSeconds * 1000).toLong()
            items.add(
                TimelineItemModel(
                    id = img.id,
                    track_type = TrackType.MAIN_MEDIA,
                    layer_index = 0,
                    startTimeMs = currentMs,
                    durationMs = durMs,
                    assetPath = img.imagePath,
                    filter_name = img.filterEffect.name,
                    transition_name = img.transition.name
                )
            )
            currentMs += durMs
        }

        // Overlay track (layer 1)
        overlays.forEachIndexed { idx, ov ->
            items.add(
                TimelineItemModel(
                    id = ov.id,
                    track_type = TrackType.OVERLAY,
                    layer_index = 1 + idx,
                    startTimeMs = (ov.startSeconds * 1000).toLong(),
                    durationMs = (ov.durationSeconds * 1000).toLong(),
                    assetPath = ov.imagePath,
                    posX = ov.posX,
                    posY = ov.posY,
                    scale = ov.scale
                )
            )
        }

        // Text captions (layer 2)
        subtitles.forEachIndexed { idx, sub ->
            items.add(
                TimelineItemModel(
                    id = sub.id,
                    track_type = TrackType.TEXT,
                    layer_index = 10 + idx,
                    startTimeMs = (sub.startSeconds * 1000).toLong(),
                    durationMs = ((sub.endSeconds - sub.startSeconds) * 1000).toLong(),
                    posX = sub.posX,
                    posY = sub.posY,
                    text_content = sub.text,
                    text_style = sub.fontName,
                    effect_name = sub.textEffect.name,
                    animation_in = sub.animationPreset.name
                )
            )
        }

        // Audio tracks
        audioTracks.forEachIndexed { idx, aud ->
            items.add(
                TimelineItemModel(
                    id = aud.id,
                    track_type = TrackType.AUDIO,
                    layer_index = idx,
                    startTimeMs = (aud.startSeconds * 1000).toLong(),
                    durationMs = (aud.durationSeconds * 1000).toLong(),
                    assetPath = aud.audioPath,
                    scale = aud.volume
                )
            )
        }

        return ProjectModel(
            id = metadata.id,
            name = metadata.title,
            canvasSize = metadata.aspectRatio.label,
            lastModified = metadata.modifiedAt,
            items = items
        )
    }
}

enum class TrackType {
    MAIN_MEDIA,
    AUDIO,
    TEXT,
    OVERLAY
}

data class TimelineItemModel(
    val id: String,
    val track_type: TrackType = TrackType.MAIN_MEDIA,
    val layer_index: Int = 0,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 3000L,
    val assetPath: String = "",
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val scale: Float = 1.0f,
    val effect_name: String = "",
    val filter_name: String = "NORMAL",
    val transition_name: String = "CROSSFADE",
    val animation_in: String = "POP_UP",
    val animation_out: String = "FADE_IN",
    val text_content: String = "",
    val text_style: String = "Anton"
)

data class ProjectModel(
    val id: String,
    val name: String,
    val canvasSize: String = "9:16",
    val lastModified: Long = System.currentTimeMillis(),
    val items: List<TimelineItemModel> = emptyList()
) {
    fun toVideoProject(): VideoProject {
        val ratio = when (canvasSize) {
            "16:9" -> ProjectAspectRatio.YOUTUBE_16_9
            "1:1" -> ProjectAspectRatio.SQUARE_1_1
            "4:5" -> ProjectAspectRatio.PORTRAIT_4_5
            else -> ProjectAspectRatio.SHORTS_9_16
        }

        val metadata = ProjectMetadata(
            id = id,
            title = name,
            createdAt = lastModified,
            modifiedAt = lastModified,
            aspectRatio = ratio
        )

        val imageList = mutableListOf<ImageSyncItem>()
        val overlayList = mutableListOf<OverlayImageItem>()
        val textList = mutableListOf<TextSyncItem>()
        val audioList = mutableListOf<AudioTrackItem>()

        items.sortedBy { it.startTimeMs }.forEach { item ->
            when (item.track_type) {
                TrackType.MAIN_MEDIA -> {
                    val trans = try {
                        TransitionType.valueOf(item.transition_name.uppercase())
                    } catch (_: Exception) {
                        TransitionType.entries.find { it.name.equals(item.transition_name, ignoreCase = true) } ?: TransitionType.CROSSFADE
                    }
                    val filt = try {
                        FilterEffectType.valueOf(item.filter_name.uppercase())
                    } catch (_: Exception) {
                        FilterEffectType.entries.find { it.name.equals(item.filter_name, ignoreCase = true) } ?: FilterEffectType.NORMAL
                    }

                    imageList.add(
                        ImageSyncItem(
                            id = item.id.ifEmpty { "img_${java.util.UUID.randomUUID().toString().take(6)}" },
                            imagePath = item.assetPath.ifEmpty { "sample_bg_1.jpg" },
                            durationSeconds = (item.durationMs / 1000.0).coerceAtLeast(0.5),
                            transition = trans,
                            filterEffect = filt
                        )
                    )
                }
                TrackType.OVERLAY -> {
                    overlayList.add(
                        OverlayImageItem(
                            id = item.id.ifEmpty { "ov_${java.util.UUID.randomUUID().toString().take(6)}" },
                            imagePath = item.assetPath,
                            startSeconds = item.startTimeMs / 1000.0,
                            durationSeconds = (item.durationMs / 1000.0).coerceAtLeast(0.3),
                            posX = item.posX,
                            posY = item.posY,
                            scale = item.scale
                        )
                    )
                }
                TrackType.TEXT -> {
                    val anim = try {
                        AnimationPreset.valueOf(item.animation_in.uppercase())
                    } catch (_: Exception) {
                        AnimationPreset.entries.find { it.name.equals(item.animation_in, ignoreCase = true) } ?: AnimationPreset.POP_UP
                    }
                    val fx = try {
                        TextEffectPreset.valueOf(item.effect_name.uppercase())
                    } catch (_: Exception) {
                        TextEffectPreset.entries.find { it.name.equals(item.effect_name, ignoreCase = true) } ?: TextEffectPreset.STROKE_3D
                    }

                    val startSec = item.startTimeMs / 1000.0
                    val durSec = (item.durationMs / 1000.0).coerceAtLeast(0.3)
                    textList.add(
                        TextSyncItem(
                            id = item.id.ifEmpty { "txt_${java.util.UUID.randomUUID().toString().take(6)}" },
                            text = item.text_content,
                            startSeconds = startSec,
                            endSeconds = startSec + durSec,
                            fontName = item.text_style.ifEmpty { "Anton" },
                            textEffect = fx,
                            animationPreset = anim,
                            posX = item.posX,
                            posY = item.posY
                        )
                    )
                }
                TrackType.AUDIO -> {
                    audioList.add(
                        AudioTrackItem(
                            id = item.id.ifEmpty { "aud_${java.util.UUID.randomUUID().toString().take(6)}" },
                            audioPath = item.assetPath.ifEmpty { "bgm_lofi" },
                            title = if (item.assetPath.isNotEmpty()) item.assetPath else "Audio Track",
                            startSeconds = item.startTimeMs / 1000.0,
                            durationSeconds = (item.durationMs / 1000.0).coerceAtLeast(1.0),
                            volume = item.scale.coerceIn(0.1f, 2.0f)
                        )
                    )
                }
            }
        }

        return VideoProject(
            metadata = metadata,
            images = imageList,
            overlays = overlayList,
            subtitles = textList,
            audioTracks = audioList
        )
    }
}
