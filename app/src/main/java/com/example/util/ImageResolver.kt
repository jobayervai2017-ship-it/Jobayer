package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import com.example.model.FramingMode
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

object ImageResolver {

    private val bitmapCache = ConcurrentHashMap<String, Bitmap>()

    /**
     * Resolves an image from multiple candidate paths or uri strings
     */
    fun resolveFile(context: Context, projectDir: File, pathOrUri: String): File? {
        val clean = pathOrUri.trim()
        if (clean.isEmpty()) return null

        // 1. Direct file path
        val directFile = File(clean)
        if (directFile.exists() && directFile.isFile && directFile.length() > 0) {
            return directFile
        }

        // 2. In project persistent_media by filename
        val filename = directFile.name
        val persistentMediaFile = File(File(projectDir, "persistent_media"), filename)
        if (persistentMediaFile.exists() && persistentMediaFile.isFile && persistentMediaFile.length() > 0) {
            return persistentMediaFile
        }

        // 3. Directly relative to project dir
        val relativeFile = File(projectDir, clean)
        if (relativeFile.exists() && relativeFile.isFile && relativeFile.length() > 0) {
            return relativeFile
        }

        // 4. In app files directory
        val inFilesDir = File(context.filesDir, clean)
        if (inFilesDir.exists() && inFilesDir.isFile && inFilesDir.length() > 0) {
            return inFilesDir
        }

        return null
    }

    /**
     * Loads a bitmap with sample sizing to avoid OOM
     */
    fun loadBitmap(
        context: Context,
        projectDir: File,
        pathOrUri: String,
        targetWidth: Int = 1080,
        targetHeight: Int = 1920,
        framingMode: FramingMode = FramingMode.CROP_TO_FILL
    ): Bitmap {
        val cacheKey = "${projectDir.name}_${File(pathOrUri).name}_${targetWidth}x${targetHeight}_${framingMode.name}"
        bitmapCache[cacheKey]?.let {
            if (!it.isRecycled) return it
        }

        val resolvedFile = resolveFile(context, projectDir, pathOrUri)
        val srcBitmap: Bitmap? = if (resolvedFile != null) {
            try {
                // Decode bounds first
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(resolvedFile.absolutePath, options)

                // Calculate inSampleSize
                var inSampleSize = 1
                while ((options.outWidth / inSampleSize) > targetWidth * 2 || (options.outHeight / inSampleSize) > targetHeight * 2) {
                    inSampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeFile(resolvedFile.absolutePath, decodeOptions)
            } catch (_: Exception) {
                null
            }
        } else if (pathOrUri.startsWith("content://")) {
            try {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

        val resultBitmap = if (srcBitmap != null) {
            scaleToFit(srcBitmap, targetWidth, targetHeight, framingMode)
        } else {
            // Graceful aesthetic fallback (Never a blank or colored empty screen)
            createPlaceholderBitmap(targetWidth, targetHeight, File(pathOrUri).nameWithoutExtension)
        }

        bitmapCache[cacheKey] = resultBitmap
        return resultBitmap
    }

    /**
     * Pre-scales bitmap to exact target dimensions using bilinear filtering
     */
    fun scaleToFit(
        src: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        framingMode: FramingMode
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val srcWidth = src.width.toFloat()
        val srcHeight = src.height.toFloat()

        if (framingMode == FramingMode.CROP_TO_FILL) {
            // Scale to fill entire rectangle, cropping excess
            val scale = maxOf(targetWidth / srcWidth, targetHeight / srcHeight)
            val scaledW = srcWidth * scale
            val scaledH = srcHeight * scale
            val dx = (targetWidth - scaledW) / 2f
            val dy = (targetHeight - scaledH) / 2f

            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(dx, dy)
            }
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(src, matrix, paint)
        } else {
            // Fit centered with dark blurred background
            val bgScale = maxOf(targetWidth / srcWidth, targetHeight / srcHeight)
            val bgMatrix = Matrix().apply {
                postScale(bgScale, bgScale)
                postTranslate((targetWidth - srcWidth * bgScale) / 2f, (targetHeight - srcHeight * bgScale) / 2f)
            }
            val bgPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 140 }
            canvas.drawBitmap(src, bgMatrix, bgPaint)

            // Dim overlay
            canvas.drawColor(0x99000000.toInt())

            // Foreground fit
            val fgScale = minOf(targetWidth / srcWidth, targetHeight / srcHeight)
            val fgW = srcWidth * fgScale
            val fgH = srcHeight * fgScale
            val fgMatrix = Matrix().apply {
                postScale(fgScale, fgScale)
                postTranslate((targetWidth - fgW) / 2f, (targetHeight - fgH) / 2f)
            }
            val fgPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(src, fgMatrix, fgPaint)
        }

        return output
    }

    /**
     * Creates a high-fidelity placeholder bitmap when an image cannot be found
     */
    private fun createPlaceholderBitmap(width: Int, height: Int, label: String): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(0xFF181A26.toInt(), 0xFF2D3250.toInt(), 0xFF121420.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Accent borders and label
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (width * 0.05f)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Scene: $label", width * 0.5f, height * 0.5f, textPaint)

        return bitmap
    }

    fun clearCache() {
        bitmapCache.values.forEach {
            if (!it.isRecycled) {
                try { it.recycle() } catch (_: Exception) {}
            }
        }
        bitmapCache.clear()
    }
}
