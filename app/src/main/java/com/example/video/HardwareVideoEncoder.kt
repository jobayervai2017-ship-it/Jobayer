package com.example.video

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import androidx.core.content.FileProvider
import com.example.model.ExportConfig
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.model.RenderProgress
import com.example.model.RenderStatus
import com.example.model.VideoProject
import com.example.util.ImageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class HardwareVideoEncoder(private val context: Context) {

    /**
     * Encodes the project into a highly-compatible MP4 file using MediaCodec + Surface + MediaMuxer
     */
    suspend fun encodeVideo(
        project: VideoProject,
        config: ExportConfig = ExportConfig(),
        onProgress: (RenderProgress) -> Unit
    ): Result<File> = withContext(Dispatchers.Default) {
        val projectDir = File(File(context.filesDir, "projects"), project.metadata.id)

        // 1. Calculate Target Dimensions (Must be even numbers)
        val ratio = project.metadata.aspectRatio
        val shortSide = config.resolution.shortSidePixels
        val pair: Pair<Int, Int> = if (ratio.width <= ratio.height) {
            val w = shortSide
            val h = (shortSide / ratio.aspectRatio).toInt()
            Pair((w / 2) * 2, (h / 2) * 2)
        } else {
            val h = shortSide
            val w = (shortSide * ratio.aspectRatio).toInt()
            Pair((w / 2) * 2, (h / 2) * 2)
        }
        val width = pair.first
        val height = pair.second

        val frameRate = config.fps.fps
        val totalDurationSeconds = project.totalDurationSeconds
        val totalFrames = maxOf(frameRate, (totalDurationSeconds * frameRate).toInt())

        onProgress(
            RenderProgress(
                status = RenderStatus.PREPARING,
                progress = 0.02f,
                message = "Pre-scaling scene bitmaps for ${width}x${height} ${frameRate}fps..."
            )
        )

        // 2. Pre-scale all scene bitmaps once to exact target dimensions
        val preScaledBitmaps = mutableListOf<Bitmap>()
        try {
            for (imageItem in project.images) {
                val bmp = ImageResolver.loadBitmap(
                    context = context,
                    projectDir = projectDir,
                    pathOrUri = imageItem.imagePath,
                    targetWidth = width,
                    targetHeight = height,
                    framingMode = imageItem.framingMode
                )
                preScaledBitmaps.add(bmp)
            }
            if (preScaledBitmaps.isEmpty()) {
                // Fallback bitmap
                val fallbackBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                Canvas(fallbackBmp).drawColor(0xFF141622.toInt())
                preScaledBitmaps.add(fallbackBmp)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Failed to prepare scene bitmaps: ${e.message}"))
        }

        val overlayBitmaps = mutableMapOf<String, Bitmap>()
        for (ov in project.overlays) {
            if (!overlayBitmaps.containsKey(ov.imagePath)) {
                try {
                    val bmp = ImageResolver.loadBitmap(context, projectDir, ov.imagePath, width / 2, height / 2, com.example.model.FramingMode.CROP_TO_FILL)
                    overlayBitmaps[ov.imagePath] = bmp
                } catch (_: Exception) {}
            }
        }

        // 3. Prepare Output File
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val tempOutputFile = File(exportDir, "render_${System.currentTimeMillis()}.mp4")
        if (tempOutputFile.exists()) tempOutputFile.delete()

        var mediaCodec: MediaCodec? = null
        var inputSurface: Surface? = null
        var mediaMuxer: MediaMuxer? = null
        var videoTrackIndex = -1
        var muxerStarted = false
        var writtenSampleCount = 0L

        try {
            // 4. Configure MediaFormat with Samsung Exynos & Universal Gallery Compatibility
            val mimeType = "video/avc" // H.264 AVC
            val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe interval (Vital for Gallery scrubbing)
                try {
                    setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
                } catch (_: Exception) {}

                try {
                    setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh)
                    if (config.resolution == ExportResolution.UHD_4K || config.fps.fps == 60) {
                        setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel51)
                    } else {
                        setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41)
                    }
                } catch (_: Exception) {}
            }

            // Create MediaCodec
            mediaCodec = MediaCodec.createEncoderByType(mimeType)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()

            // Create MediaMuxer
            mediaMuxer = MediaMuxer(tempOutputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val bufferInfo = MediaCodec.BufferInfo()
            val startTimeNs = System.nanoTime()

            onProgress(
                RenderProgress(
                    status = RenderStatus.ENCODING,
                    progress = 0.05f,
                    currentFrame = 0,
                    totalFrames = totalFrames,
                    fps = 0f,
                    message = "Encoding frames with Exynos hardware acceleration..."
                )
            )

            // 5. Render Frame Loop
            for (frameIndex in 0 until totalFrames) {
                // Request IDR keyframe at start
                if (frameIndex == 0) {
                    try {
                        val params = android.os.Bundle().apply {
                            putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                        }
                        mediaCodec.setParameters(params)
                    } catch (_: Exception) {}
                }

                val currentTimeSeconds = frameIndex.toDouble() / frameRate.toDouble()

                // Lock Canvas on MediaCodec input Surface
                val canvas: Canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }

                try {
                    // Render Background Scene & Transitions
                    VideoRenderEffects.renderScenes(
                        canvas = canvas,
                        images = project.images,
                        bitmaps = preScaledBitmaps,
                        currentTimeSeconds = currentTimeSeconds,
                        width = width,
                        height = height
                    )

                    // Render Overlays / PIP
                    if (project.overlays.isNotEmpty() && overlayBitmaps.isNotEmpty()) {
                        VideoRenderEffects.renderOverlays(
                            canvas = canvas,
                            overlays = project.overlays,
                            overlayBitmaps = overlayBitmaps,
                            currentTimeSeconds = currentTimeSeconds,
                            width = width,
                            height = height
                        )
                    }

                    // Render Animated Subtitles
                    VideoRenderEffects.renderSubtitles(
                        canvas = canvas,
                        subtitles = project.subtitles,
                        currentTimeSeconds = currentTimeSeconds,
                        width = width,
                        height = height,
                        scaleFactor = width.toFloat() / 1080f
                    )
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain Encoder Output (Non-blocking)
                drainEncoder(
                    codec = mediaCodec,
                    muxer = mediaMuxer,
                    bufferInfo = bufferInfo,
                    getTrackIndex = { videoTrackIndex },
                    setTrackIndex = { videoTrackIndex = it },
                    isMuxerStarted = { muxerStarted },
                    setMuxerStarted = { muxerStarted = it },
                    frameRate = frameRate,
                    getWrittenCount = { writtenSampleCount },
                    incrementWrittenCount = { writtenSampleCount++ },
                    endOfStream = false
                )

                // Update Progress periodically
                if (frameIndex % 15 == 0 || frameIndex == totalFrames - 1) {
                    val progressVal = 0.05f + (frameIndex.toFloat() / totalFrames.toFloat()) * 0.85f
                    val elapsedSeconds = (System.nanoTime() - startTimeNs) / 1_000_000_000.0
                    val currentFps = if (elapsedSeconds > 0) (frameIndex / elapsedSeconds).toFloat() else 0f

                    onProgress(
                        RenderProgress(
                            status = RenderStatus.ENCODING,
                            progress = progressVal,
                            currentFrame = frameIndex + 1,
                            totalFrames = totalFrames,
                            fps = currentFps,
                            message = "Rendering frame ${frameIndex + 1}/$totalFrames (${currentFps.toInt()} fps)"
                        )
                    )
                }
            }

            // 6. Signal End of Stream
            mediaCodec.signalEndOfInputStream()

            // Drain remaining frames
            drainEncoder(
                codec = mediaCodec,
                muxer = mediaMuxer,
                bufferInfo = bufferInfo,
                getTrackIndex = { videoTrackIndex },
                setTrackIndex = { videoTrackIndex = it },
                isMuxerStarted = { muxerStarted },
                setMuxerStarted = { muxerStarted = it },
                frameRate = frameRate,
                getWrittenCount = { writtenSampleCount },
                incrementWrittenCount = { writtenSampleCount++ },
                endOfStream = true
            )

            onProgress(
                RenderProgress(
                    status = RenderStatus.SAVING_TO_GALLERY,
                    progress = 0.95f,
                    message = "Finalizing MP4 and saving to Samsung Gallery..."
                )
            )

        } finally {
            // Safe cleanup order: inputSurface -> codec stop/release -> muxer stop/release
            try { inputSurface?.release() } catch (_: Exception) {}
            try { mediaCodec?.stop() } catch (_: Exception) {}
            try { mediaCodec?.release() } catch (_: Exception) {}

            try {
                if (muxerStarted) {
                    mediaMuxer?.stop()
                }
            } catch (_: Exception) {}
            try { mediaMuxer?.release() } catch (_: Exception) {}
        }

        // Verify Output File
        if (!tempOutputFile.exists() || tempOutputFile.length() < 1024) {
            return@withContext Result.failure(Exception("Encoded MP4 file is empty or missing."))
        }

        // 7. Save directly to Android/Samsung MediaStore under Movies/FacelessEditor/
        val mediaStoreUri = saveToMediaStore(tempOutputFile, project.metadata.title)

        // Keep a copy in permanent exports dir for direct in-app playback and sharing
        val finalPermanentFile = File(File(context.filesDir, "exports"), "SyncCut_${System.currentTimeMillis()}.mp4")
        tempOutputFile.copyTo(finalPermanentFile, overwrite = true)

        onProgress(
            RenderProgress(
                status = RenderStatus.COMPLETED,
                progress = 1.0f,
                currentFrame = totalFrames,
                totalFrames = totalFrames,
                message = "Video exported successfully! Available in Gallery.",
                outputFilePath = finalPermanentFile.absolutePath,
                mediaStoreUri = mediaStoreUri
            )
        )

        Result.success(finalPermanentFile)
    }

    /**
     * Drains encoded packets from MediaCodec to MediaMuxer
     */
    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        getTrackIndex: () -> Int,
        setTrackIndex: (Int) -> Unit,
        isMuxerStarted: () -> Boolean,
        setMuxerStarted: (Boolean) -> Unit,
        frameRate: Int,
        getWrittenCount: () -> Long,
        incrementWrittenCount: () -> Unit,
        endOfStream: Boolean
    ) {
        val timeoutUs = if (endOfStream) 10_000L else 0L

        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (isMuxerStarted()) {
                    // Ignore format changed once muxer has started
                } else {
                    val newFormat = codec.outputFormat
                    val trackIndex = muxer.addTrack(newFormat)
                    setTrackIndex(trackIndex)
                    muxer.start()
                    setMuxerStarted(true)
                }
            } else if (outputBufferIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // Codec config data already consumed in INFO_OUTPUT_FORMAT_CHANGED
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size != 0 && isMuxerStarted()) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                        // Enforce strictly monotonic PTS at exactly target frame rate
                        val currentSample = getWrittenCount()
                        bufferInfo.presentationTimeUs = (currentSample * 1_000_000L) / frameRate
                        muxer.writeSampleData(getTrackIndex(), outputBuffer, bufferInfo)
                        incrementWrittenCount()
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    /**
     * Inserts the completed MP4 into MediaStore under Movies/FacelessEditor/
     * and triggers MediaScanner so Samsung Gallery refreshes instantly!
     */
    private fun saveToMediaStore(sourceFile: File, projectTitle: String): Uri? {
        val cleanTitle = projectTitle.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val displayName = "SyncCut_${cleanTitle}_${System.currentTimeMillis()}.mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/FacelessEditor/")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val insertedUri = resolver.insert(collectionUri, values) ?: return null

        try {
            resolver.openOutputStream(insertedUri)?.use { out ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(insertedUri, values, null, null)
            }

            // Also trigger media scanner
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(sourceFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
            } catch (_: Exception) {}

            return insertedUri
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Launches external Gallery/Video Player to view the exported video
     */
    fun openVideoInGallery(context: Context, file: File, mediaStoreUri: Uri?) {
        val uri = mediaStoreUri ?: FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Play Video").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {}
    }

    /**
     * Shares the exported video to social media, messaging apps, etc.
     */
    fun shareVideo(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Share Faceless Video").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {}
    }
}
