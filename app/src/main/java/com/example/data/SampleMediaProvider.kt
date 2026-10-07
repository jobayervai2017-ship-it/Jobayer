package com.example.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import java.io.File
import java.io.FileOutputStream

object SampleMediaProvider {

    /**
     * Generates a starter set of 4 vibrant cinematic background images in persistent_media/
     */
    fun createSampleImages(targetDir: File, width: Int = 1080, height: Int = 1920): List<String> {
        targetDir.mkdirs()
        val filenames = listOf(
            "sample_cinematic_1.jpg",
            "sample_cinematic_2.jpg",
            "sample_cinematic_3.jpg",
            "sample_cinematic_4.jpg"
        )

        val createdPaths = mutableListOf<String>()

        val gradients = listOf(
            // 1. Cyber Purple & Dark Indigo
            intArrayOf(0xFF1E1B4B.toInt(), 0xFF4338CA.toInt(), 0xFF6366F1.toInt(), 0xFF0A0A14.toInt()),
            // 2. Sunset Orange & Deep Crimson
            intArrayOf(0xFF7C2D12.toInt(), 0xFFDC2626.toInt(), 0xFFF59E0B.toInt(), 0xFF180A0A.toInt()),
            // 3. Cyber Emerald & Deep Teal
            intArrayOf(0xFF064E3B.toInt(), 0xFF059669.toInt(), 0xFF10B981.toInt(), 0xFF051814.toInt()),
            // 4. Electric Cyan & Obsidian Midnight
            intArrayOf(0xFF083344.toInt(), 0xFF0284C7.toInt(), 0xFF38BDF8.toInt(), 0xFF0A0F1A.toInt())
        )

        for (i in filenames.indices) {
            val file = File(targetDir, filenames[i])
            if (!file.exists() || file.length() == 0L) {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                // Background gradient
                val colors = gradients[i]
                val shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    colors,
                    floatArrayOf(0f, 0.4f, 0.75f, 1f),
                    Shader.TileMode.CLAMP
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Atmospheric radial flare
                val radialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.shader = RadialGradient(
                        width * 0.5f,
                        height * (0.3f + i * 0.12f),
                        width * 0.65f,
                        intArrayOf(0x55FFFFFF, 0x11FFFFFF, 0x00000000),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), radialPaint)

                // Subtle studio geometric motif
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0x22FFFFFF
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                }
                canvas.drawCircle(width * 0.5f, height * 0.5f, width * 0.35f, linePaint)
                canvas.drawCircle(width * 0.5f, height * 0.5f, width * 0.45f, linePaint)

                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                bitmap.recycle()
            }
            createdPaths.add(file.absolutePath)
        }

        return createdPaths
    }
}
