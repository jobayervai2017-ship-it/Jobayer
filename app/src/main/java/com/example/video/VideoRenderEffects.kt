package com.example.video

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.AnimationPreset
import com.example.model.FilterEffectType
import com.example.model.ImageSyncItem
import com.example.model.OverlayImageItem
import com.example.model.TextEffectPreset
import com.example.model.TextSyncItem
import com.example.model.TransitionType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

object VideoRenderEffects {

    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val textFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val textStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
    }
    private val textShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val dstRect = Rect()
    private val boxRect = RectF()

    private fun drawFitted(
        canvas: Canvas,
        bitmap: Bitmap,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        paint: Paint
    ) {
        val targetW = right - left
        val targetH = bottom - top
        if (targetW <= 0 || targetH <= 0 || bitmap.width <= 0 || bitmap.height <= 0) return

        val bW = bitmap.width.toFloat()
        val bH = bitmap.height.toFloat()

        // BoxFit.contain: Scale to fit inside the canvas bounds preserving aspect ratio without cropping
        val scale = minOf(targetW.toFloat() / bW, targetH.toFloat() / bH)
        val fitW = (bW * scale).toInt()
        val fitH = (bH * scale).toInt()
        val offsetX = left + (targetW - fitW) / 2
        val offsetY = top + (targetH - fitH) / 2

        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
        dstRect.set(offsetX, offsetY, offsetX + fitW, offsetY + fitH)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
    }

    /**
     * Color Filter matrix evaluation for all 36 cinematic grades
     */
    fun getColorFilter(type: FilterEffectType): ColorMatrixColorFilter? {
        val cm = ColorMatrix()
        return when (type) {
            FilterEffectType.NORMAL -> null
            FilterEffectType.CINEMATIC_WARM -> {
                cm.set(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 15f,
                    0f, 1.05f, 0f, 0f, 5f,
                    0f, 0f, 0.85f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.TEAL_ORANGE -> {
                cm.set(floatArrayOf(
                    1.25f, 0f, 0f, 0f, 15f,
                    0f, 1.0f, 0.1f, 0f, 0f,
                    -0.1f, 0.1f, 1.15f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.COLD_STEEL -> {
                cm.set(floatArrayOf(
                    0.85f, 0f, 0.1f, 0f, -10f,
                    0f, 1.0f, 0.1f, 0f, 0f,
                    0.1f, 0.1f, 1.35f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.BLEACH_BYPASS -> {
                cm.setSaturation(0.4f)
                val c = 1.35f
                val t = (-0.5f * c + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    c, 0f, 0f, 0f, t,
                    0f, c, 0f, 0f, t,
                    0f, 0f, c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.GOLDEN_HOUR -> {
                cm.set(floatArrayOf(
                    1.3f, 0f, 0f, 0f, 22f,
                    0f, 1.1f, 0f, 0f, 10f,
                    0f, 0f, 0.75f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.DESERT_WARMTH -> {
                cm.set(floatArrayOf(
                    1.25f, 0.05f, 0f, 0f, 20f,
                    0f, 1.05f, 0f, 0f, 5f,
                    0f, 0f, 0.7f, 0f, -25f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.VINTAGE_VHS -> {
                cm.setSaturation(0.75f)
                val tint = ColorMatrix(floatArrayOf(
                    1f, 0f, 0f, 0f, 8f,
                    0f, 1.05f, 0f, 0f, 12f,
                    0f, 0f, 0.9f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(tint)
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.RETRO_90S -> {
                cm.setSaturation(0.85f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.1f, 0.05f, 0f, 0f, 12f,
                    0f, 1.0f, 0.05f, 0f, 8f,
                    0f, 0f, 0.95f, 0f, 4f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.FILM_GRAIN -> {
                cm.set(floatArrayOf(
                    1.05f, 0.05f, 0f, 0f, 5f,
                    0.05f, 1.05f, 0.05f, 0f, 5f,
                    0f, 0.05f, 1.05f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.SEPIA_CLASSIC -> {
                cm.set(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.KODAK_CHROME -> {
                cm.setSaturation(1.3f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.12f, 0f, 0f, 0f, 8f,
                    0f, 1.08f, 0f, 0f, 4f,
                    0f, 0f, 0.98f, 0f, -4f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.FUJI_VELVIA -> {
                cm.setSaturation(1.4f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.05f, 0f, 0f, 0f, 0f,
                    0f, 1.2f, 0f, 0f, 10f,
                    0f, 0f, 1.15f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.CYBERPUNK -> {
                cm.set(floatArrayOf(
                    1.15f, 0f, 0.2f, 0f, 10f,
                    0f, 0.95f, 0.2f, 0f, -10f,
                    0.2f, 0.2f, 1.35f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.MATRIX_GREEN -> {
                cm.set(floatArrayOf(
                    0.3f, 0.2f, 0f, 0f, -20f,
                    0.1f, 1.45f, 0.1f, 0f, 35f,
                    0f, 0.2f, 0.3f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.NEON_PURPLE -> {
                cm.set(floatArrayOf(
                    1.25f, 0f, 0.25f, 0f, 15f,
                    0f, 0.8f, 0.1f, 0f, -10f,
                    0.2f, 0.1f, 1.4f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.INFRARED_RED -> {
                cm.set(floatArrayOf(
                    1.6f, 0f, 0f, 0f, 40f,
                    0f, 0.5f, 0f, 0f, -20f,
                    0f, 0f, 0.5f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.SOLARIZE -> {
                cm.set(floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, 1.2f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.BLACK_WHITE -> {
                cm.setSaturation(0f)
                val c = 1.25f
                val t = (-0.5f * c + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    c, 0f, 0f, 0f, t,
                    0f, c, 0f, 0f, t,
                    0f, 0f, c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.NOIR_CONTRAST -> {
                cm.setSaturation(0f)
                val c = 1.45f
                val t = (-0.5f * c + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    c, 0f, 0f, 0f, t,
                    0f, c, 0f, 0f, t,
                    0f, 0f, c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.MOODY_FOREST -> {
                cm.setSaturation(0.9f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    0.85f, 0f, 0f, 0f, -15f,
                    0f, 1.15f, 0f, 0f, 10f,
                    0f, 0.1f, 0.95f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.NIGHT_VISION -> {
                cm.set(floatArrayOf(
                    0.1f, 0.1f, 0f, 0f, 0f,
                    0.3f, 1.5f, 0.3f, 0f, 40f,
                    0f, 0.1f, 0.1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.VIGNETTE_DRAMA -> {
                cm.setSaturation(1.1f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.1f, 0f, 0f, 0f, -10f,
                    0f, 1.1f, 0f, 0f, -10f,
                    0f, 0f, 1.1f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.MONOCHROME_RED -> {
                cm.set(floatArrayOf(
                    1.5f, 0f, 0f, 0f, 20f,
                    0.2f, 0.2f, 0.2f, 0f, -30f,
                    0.2f, 0.2f, 0.2f, 0f, -30f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.HDR_VIVID -> {
                cm.setSaturation(1.35f)
                val c = 1.15f
                val t = (-0.5f * c + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    c, 0f, 0f, 0f, t + 10f,
                    0f, c, 0f, 0f, t + 10f,
                    0f, 0f, c, 0f, t + 10f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.ANIME_POP -> {
                cm.setSaturation(1.5f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.15f, 0f, 0f, 0f, 15f,
                    0f, 1.15f, 0f, 0f, 15f,
                    0f, 0f, 1.15f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.SUNSET_BLAZE -> {
                cm.set(floatArrayOf(
                    1.35f, 0.1f, 0f, 0f, 30f,
                    0f, 1.0f, 0.1f, 0f, 5f,
                    0.1f, 0f, 0.8f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.AQUA_DEEP -> {
                cm.set(floatArrayOf(
                    0.85f, 0f, 0f, 0f, -15f,
                    0f, 1.15f, 0.2f, 0f, 15f,
                    0f, 0.2f, 1.35f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.PEACHY_VIBE -> {
                cm.set(floatArrayOf(
                    1.2f, 0.1f, 0f, 0f, 20f,
                    0f, 1.05f, 0.05f, 0f, 10f,
                    0f, 0.05f, 0.95f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.LOFI_CHILL -> {
                cm.setSaturation(0.8f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.05f, 0f, 0f, 0f, 12f,
                    0f, 1.02f, 0f, 0f, 6f,
                    0f, 0f, 0.98f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.DREAMY_GLOW -> {
                cm.set(floatArrayOf(
                    1.08f, 0.04f, 0.04f, 0f, 20f,
                    0.04f, 1.08f, 0.04f, 0f, 20f,
                    0.04f, 0.04f, 1.08f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.PASTEL_CREAM -> {
                cm.setSaturation(0.85f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.05f, 0f, 0f, 0f, 15f,
                    0f, 1.05f, 0f, 0f, 15f,
                    0f, 0f, 1.0f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.EMERALD_MINT -> {
                cm.set(floatArrayOf(
                    0.9f, 0f, 0f, 0f, -10f,
                    0f, 1.25f, 0.1f, 0f, 25f,
                    0f, 0.1f, 1.15f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.ROSE_GOLD -> {
                cm.set(floatArrayOf(
                    1.25f, 0.05f, 0f, 0f, 20f,
                    0f, 1.0f, 0.05f, 0f, 5f,
                    0.1f, 0f, 1.15f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.FROST_ICE -> {
                cm.set(floatArrayOf(
                    0.85f, 0f, 0.1f, 0f, 5f,
                    0f, 1.05f, 0.15f, 0f, 15f,
                    0.1f, 0.15f, 1.35f, 0f, 35f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.HOLLYWOOD_PRIME -> {
                cm.set(floatArrayOf(
                    1.15f, 0f, 0f, 0f, 10f,
                    0f, 1.05f, 0f, 0f, 5f,
                    0f, 0f, 0.9f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.INDIE_DIRECTOR -> {
                cm.set(floatArrayOf(
                    0.95f, 0.05f, 0f, 0f, -5f,
                    0f, 1.1f, 0.05f, 0f, 8f,
                    0.05f, 0.05f, 0.95f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.EPIC_SCI_FI -> {
                cm.set(floatArrayOf(
                    0.8f, 0f, 0.1f, 0f, -15f,
                    0f, 1.0f, 0.15f, 0f, 0f,
                    0.1f, 0.15f, 1.4f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                ))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.POLAROID_600, FilterEffectType.SUPER_8MM, FilterEffectType.AGFA_VINTAGE -> {
                cm.setSaturation(0.85f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    1.15f, 0.05f, 0f, 0f, 15f,
                    0f, 1.05f, 0f, 0f, 10f,
                    0f, 0f, 0.85f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.ACID_NEON, FilterEffectType.CYBER_GLOW -> {
                cm.setSaturation(1.6f)
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.GOTHIC_DARK, FilterEffectType.MIDNIGHT_BLUE -> {
                cm.setSaturation(0.6f)
                cm.postConcat(ColorMatrix(floatArrayOf(
                    0.8f, 0f, 0f, 0f, -20f,
                    0f, 0.85f, 0f, 0f, -10f,
                    0f, 0f, 1.2f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.VIRAL_BOOST, FilterEffectType.CANDY_COLOR -> {
                cm.setSaturation(1.5f)
                val c = 1.1f
                val t = (-0.5f * c + 0.5f) * 255f
                cm.postConcat(ColorMatrix(floatArrayOf(
                    c, 0f, 0f, 0f, t + 12f,
                    0f, c, 0f, 0f, t + 12f,
                    0f, 0f, c, 0f, t + 12f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.COZY_COFFEE, FilterEffectType.LAVENDER_DUSK -> {
                cm.setSaturation(0.9f)
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.SUMMER_BREEZE, FilterEffectType.AUTUMN_LEAVES, FilterEffectType.JUNGLE_DEEP, FilterEffectType.DESERT_DUNE -> {
                cm.setSaturation(1.25f)
                ColorMatrixColorFilter(cm)
            }
            FilterEffectType.DUOTONE_CYAN_MAGENTA, FilterEffectType.DUOTONE_YELLOW_NAVY, FilterEffectType.DUOTONE_GOLD_BLACK, FilterEffectType.DUOTONE_PURPLE_ORANGE -> {
                cm.setSaturation(0.2f)
                ColorMatrixColorFilter(cm)
            }
            else -> {
                when (type.category) {
                    com.example.model.FilterCategory.CINEMATIC -> {
                        cm.setSaturation(1.15f)
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.RETRO_FILM -> {
                        cm.setSaturation(0.85f)
                        val c = 1.05f
                        val t = (-0.5f * c + 0.5f) * 255f
                        cm.postConcat(ColorMatrix(floatArrayOf(
                            c, 0f, 0f, 0f, t + 10f,
                            0f, c, 0f, 0f, t + 8f,
                            0f, 0f, c, 0f, t - 6f,
                            0f, 0f, 0f, 1f, 0f
                        )))
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.CYBER_NEON -> {
                        cm.setSaturation(1.5f)
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.MOODY_NOIR -> {
                        cm.setSaturation(0.12f)
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.VIBRANT_POP -> {
                        cm.setSaturation(1.4f)
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.AESTHETIC_LOFI -> {
                        cm.setSaturation(0.88f)
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.NATURE_SEASONAL -> {
                        cm.setSaturation(1.22f)
                        ColorMatrixColorFilter(cm)
                    }
                    com.example.model.FilterCategory.DUOTONE_CREATIVE -> {
                        cm.setSaturation(0.18f)
                        ColorMatrixColorFilter(cm)
                    }
                }
            }
        }
    }

    /**
     * Renders background scene (with transitions) onto the canvas
     */
    fun renderScenes(
        canvas: Canvas,
        images: List<ImageSyncItem>,
        bitmaps: List<Bitmap>,
        currentTimeSeconds: Double,
        width: Int,
        height: Int
    ) {
        if (images.isEmpty() || bitmaps.isEmpty()) {
            canvas.drawColor(0xFF0F111A.toInt())
            return
        }

        var cumulativeTime = 0.0
        var activeIndex = images.size - 1
        var sceneProgress = 0.0

        for (i in images.indices) {
            val dur = images[i].durationSeconds
            if (currentTimeSeconds >= cumulativeTime && currentTimeSeconds < cumulativeTime + dur) {
                activeIndex = i
                sceneProgress = (currentTimeSeconds - cumulativeTime) / dur
                break
            }
            cumulativeTime += dur
        }

        val currentScene = images[activeIndex]
        val currentBitmap = bitmaps.getOrElse(activeIndex) { bitmaps.first() }

        val transitionDuration = currentScene.transition.defaultDuration
        val timeRemainingInScene = (1.0 - sceneProgress) * currentScene.durationSeconds
        val isInTransition = (activeIndex < images.size - 1) &&
                (currentScene.transition != TransitionType.NONE) &&
                (timeRemainingInScene < transitionDuration)

        if (!isInTransition) {
            bitmapPaint.reset()
            bitmapPaint.isFilterBitmap = true
            bitmapPaint.isAntiAlias = true
            bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)

            // Dynamic Motion Animation (Supporting all 200+ Motion Styles)
            var animScale = 1.0f
            var animTransX = 0f
            var animTransY = 0f
            var animRotation = 0f

            when (currentScene.motionAnimation.category) {
                com.example.model.MotionCategory.KEN_BURNS_ZOOM -> {
                    if (currentScene.motionAnimation.name.contains("OUT") || currentScene.motionAnimation.name.contains("PULL")) {
                        animScale = 1.18f - (sceneProgress * 0.16f).toFloat()
                    } else if (currentScene.motionAnimation == com.example.model.ImageMotionAnimation.NONE) {
                        animScale = 1.0f
                    } else {
                        animScale = 1.0f + (sceneProgress * 0.16f).toFloat()
                    }
                }
                com.example.model.MotionCategory.CAMERA_PAN -> {
                    animScale = 1.12f
                    val sign = if (currentScene.motionAnimation.name.contains("RIGHT")) 1f else -1f
                    val speed = if (currentScene.motionAnimation.name.contains("FAST") || currentScene.motionAnimation.name.contains("WHIP")) 85f else 50f
                    animTransX = (sign * sceneProgress * speed).toFloat()
                }
                com.example.model.MotionCategory.CAMERA_TILT -> {
                    animScale = 1.12f
                    val sign = if (currentScene.motionAnimation.name.contains("DOWN") || currentScene.motionAnimation.name.contains("DROP")) 1f else -1f
                    val speed = if (currentScene.motionAnimation.name.contains("FAST") || currentScene.motionAnimation.name.contains("DIVE")) 65f else 40f
                    animTransY = (sign * sceneProgress * speed).toFloat()
                }
                com.example.model.MotionCategory.DYNAMIC_SHAKE -> {
                    animScale = 1.06f
                    val freq = if (currentScene.motionAnimation.name.contains("EARTHQUAKE") || currentScene.motionAnimation.name.contains("IMPACT")) 24.0 else 16.0
                    val amp = if (currentScene.motionAnimation.name.contains("EARTHQUAKE") || currentScene.motionAnimation.name.contains("BASS")) 6.0 else 3.5
                    animTransX = (sin(currentTimeSeconds * freq) * amp).toFloat()
                    animTransY = (cos(currentTimeSeconds * (freq * 0.85)) * amp).toFloat()
                }
                com.example.model.MotionCategory.PULSE_RHYTHM -> {
                    val beats = if (currentScene.motionAnimation.name.contains("HEARTBEAT") || currentScene.motionAnimation.name.contains("EDM")) 4.0 else 2.0
                    animScale = (1.0f + sin(sceneProgress * PI * beats) * 0.08f).toFloat().coerceAtLeast(1.0f)
                }
                com.example.model.MotionCategory.DIAGONAL_DRIFT -> {
                    animScale = 1.10f
                    val xDir = if (currentScene.motionAnimation.name.contains("RIGHT")) 1f else -1f
                    val yDir = if (currentScene.motionAnimation.name.contains("DOWN") || currentScene.motionAnimation.name.contains("BOTTOM")) 1f else -1f
                    animTransX = (xDir * sceneProgress * 35f).toFloat()
                    animTransY = (yDir * sceneProgress * 30f).toFloat()
                }
                com.example.model.MotionCategory.ROTATION_ORBIT -> {
                    animScale = 1.14f
                    val dir = if (currentScene.motionAnimation.name.contains("CCW") || currentScene.motionAnimation.name.contains("LEFT")) -1f else 1f
                    animRotation = (dir * sceneProgress * 10f).toFloat()
                }
                com.example.model.MotionCategory.WARP_3D_KINETIC -> {
                    animScale = 1.0f + (sceneProgress * 0.20f).toFloat()
                    animTransX = (sin(sceneProgress * PI * 2) * 12f).toFloat()
                }
            }

            if (animScale != 1.0f || animTransX != 0f || animTransY != 0f || animRotation != 0f) {
                canvas.save()
                canvas.translate(width * 0.5f + animTransX, height * 0.5f + animTransY)
                canvas.scale(animScale, animScale)
                if (animRotation != 0f) {
                    canvas.rotate(animRotation)
                }
                drawFitted(canvas, currentBitmap, -width / 2, -height / 2, width / 2, height / 2, bitmapPaint)
                canvas.restore()
            } else {
                drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
            }
        } else {
            val nextScene = images[activeIndex + 1]
            val nextBitmap = bitmaps.getOrElse(activeIndex + 1) { currentBitmap }
            val t = ((transitionDuration - timeRemainingInScene) / transitionDuration).toFloat().coerceIn(0f, 1f)

            when (currentScene.transition) {
                TransitionType.NONE -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                }

                TransitionType.CROSSFADE -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    bitmapPaint.alpha = 255
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                }

                TransitionType.DIP_TO_BLACK -> {
                    if (t < 0.5f) {
                        val fadeOut = (1f - t * 2f).coerceIn(0f, 1f)
                        bitmapPaint.reset()
                        bitmapPaint.isFilterBitmap = true
                        bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                        bitmapPaint.alpha = (fadeOut * 255).toInt()
                        drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    } else {
                        val fadeIn = ((t - 0.5f) * 2f).coerceIn(0f, 1f)
                        bitmapPaint.reset()
                        bitmapPaint.isFilterBitmap = true
                        bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                        bitmapPaint.alpha = (fadeIn * 255).toInt()
                        drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    }
                }

                TransitionType.DIP_TO_WHITE -> {
                    if (t < 0.5f) {
                        bitmapPaint.reset()
                        bitmapPaint.isFilterBitmap = true
                        bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                        drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                        val whiteAlpha = (t * 2f * 255).toInt()
                        canvas.drawColor((whiteAlpha shl 24) or 0x00FFFFFF)
                    } else {
                        bitmapPaint.reset()
                        bitmapPaint.isFilterBitmap = true
                        bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                        drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                        val whiteAlpha = ((1f - t) * 2f * 255).toInt()
                        canvas.drawColor((whiteAlpha shl 24) or 0x00FFFFFF)
                    }
                }

                TransitionType.SLIDE_LEFT, TransitionType.WHIP_LEFT -> {
                    val offset = (t * width).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, -offset, 0, width - offset, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    drawFitted(canvas, nextBitmap, width - offset, 0, 2 * width - offset, height, bitmapPaint)
                }

                TransitionType.SLIDE_RIGHT, TransitionType.WHIP_RIGHT -> {
                    val offset = (t * width).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, offset, 0, width + offset, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    drawFitted(canvas, nextBitmap, -width + offset, 0, offset, height, bitmapPaint)
                }

                TransitionType.SLIDE_UP -> {
                    val offset = (t * height).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, -offset, width, height - offset, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    drawFitted(canvas, nextBitmap, 0, height - offset, width, 2 * height - offset, bitmapPaint)
                }

                TransitionType.SLIDE_DOWN -> {
                    val offset = (t * height).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, offset, width, height + offset, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    drawFitted(canvas, nextBitmap, 0, -height + offset, width, offset, bitmapPaint)
                }

                TransitionType.SPIN_CLOCKWISE -> {
                    canvas.save()
                    canvas.rotate(t * 180f, width * 0.5f, height * 0.5f)
                    val scale = 1.0f - t * 0.4f
                    canvas.scale(scale, scale, width * 0.5f, height * 0.5f)
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    bitmapPaint.alpha = ((1f - t) * 255).toInt()
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()

                    canvas.save()
                    canvas.rotate(-180f + t * 180f, width * 0.5f, height * 0.5f)
                    val scaleNext = 0.6f + t * 0.4f
                    canvas.scale(scaleNext, scaleNext, width * 0.5f, height * 0.5f)
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.SPIN_COUNTER -> {
                    canvas.save()
                    canvas.rotate(-t * 180f, width * 0.5f, height * 0.5f)
                    val scale = 1.0f - t * 0.4f
                    canvas.scale(scale, scale, width * 0.5f, height * 0.5f)
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    bitmapPaint.alpha = ((1f - t) * 255).toInt()
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()

                    canvas.save()
                    canvas.rotate(180f - t * 180f, width * 0.5f, height * 0.5f)
                    val scaleNext = 0.6f + t * 0.4f
                    canvas.scale(scaleNext, scaleNext, width * 0.5f, height * 0.5f)
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.VERTICAL_SPLIT -> {
                    val half = height / 2
                    val split = (t * half).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    // Top half moves up, bottom half moves down
                    canvas.save()
                    canvas.clipRect(0, 0, width, half)
                    drawFitted(canvas, currentBitmap, 0, -split, width, height - split, bitmapPaint)
                    canvas.restore()

                    canvas.save()
                    canvas.clipRect(0, half, width, height)
                    drawFitted(canvas, currentBitmap, 0, split, width, height + split, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.HORIZONTAL_SPLIT -> {
                    val half = width / 2
                    val split = (t * half).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    canvas.save()
                    canvas.clipRect(0, 0, half, height)
                    drawFitted(canvas, currentBitmap, -split, 0, width - split, height, bitmapPaint)
                    canvas.restore()

                    canvas.save()
                    canvas.clipRect(half, 0, width, height)
                    drawFitted(canvas, currentBitmap, split, 0, width + split, height, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.MIRROR_FLIP -> {
                    canvas.save()
                    canvas.scale(1f - t * 2f, 1f, width * 0.5f, height * 0.5f)
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    if (t < 0.5f) {
                        bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                        drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    } else {
                        bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                        drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    }
                    canvas.restore()
                }

                TransitionType.ZOOM_IN, TransitionType.SCALE_UP -> {
                    canvas.save()
                    val scale = 1.0f + t * 0.35f
                    canvas.scale(scale, scale, width * 0.5f, height * 0.5f)
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                }

                TransitionType.ZOOM_OUT, TransitionType.SCALE_DOWN -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    canvas.save()
                    val s = 1.4f - t * 0.4f
                    canvas.scale(s, s, width * 0.5f, height * 0.5f)
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.BLUR_ZOOM, TransitionType.WARP_SPEED -> {
                    canvas.save()
                    val scale = 1.0f + t * 0.5f
                    canvas.scale(scale, scale, width * 0.5f, height * 0.5f)
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    bitmapPaint.alpha = ((1f - t) * 255).toInt()
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()

                    canvas.save()
                    val scaleNext = 0.8f + t * 0.2f
                    canvas.scale(scaleNext, scaleNext, width * 0.5f, height * 0.5f)
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.IRIS_CIRCLE -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    canvas.save()
                    val radius = t * (Math.hypot(width.toDouble(), height.toDouble()) * 0.55).toFloat()
                    val path = android.graphics.Path()
                    path.addCircle(width * 0.5f, height * 0.5f, radius, android.graphics.Path.Direction.CW)
                    canvas.clipPath(path)
                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = 255
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()
                }

                TransitionType.FILM_BURN -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)

                    val flashAlpha = (sin(t.toDouble() * PI) * 180).toInt()
                    canvas.drawColor((flashAlpha shl 24) or 0x00FF8800)
                }

                TransitionType.GLITCH_BURST -> {
                    val jitterX = ((Math.random() - 0.5) * 35.0 * sin(t.toDouble() * PI)).toInt()
                    val jitterY = ((Math.random() - 0.5) * 20.0 * sin(t.toDouble() * PI)).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    if (t < 0.5f) {
                        bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                        drawFitted(canvas, currentBitmap, jitterX, jitterY, width + jitterX, height + jitterY, bitmapPaint)
                    } else {
                        bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                        drawFitted(canvas, nextBitmap, jitterX, jitterY, width + jitterX, height + jitterY, bitmapPaint)
                    }
                }

                TransitionType.SHAKE_PUNCH -> {
                    val shake = (sin(t * 20.0) * (1f - t) * 25.0).toInt()
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, shake, 0, width + shake, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                }

                TransitionType.PIXEL_DISSOLVE, TransitionType.WAVE_DISTORT -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                }

                TransitionType.FLASH_WHITE -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    if (t < 0.4f) {
                        bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                        drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)
                    } else {
                        bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                        drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    }
                    val flashAlpha = (sin(t.toDouble() * PI) * 255).toInt()
                    canvas.drawColor((flashAlpha shl 24) or 0x00FFFFFF)
                }

                TransitionType.LIGHT_LEAK -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)

                    val leakAlpha = (sin(t.toDouble() * PI) * 160).toInt()
                    canvas.drawColor((leakAlpha shl 24) or 0x00FFAA33)
                }

                TransitionType.COLOR_PULSE -> {
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)

                    val pulseAlpha = (sin(t.toDouble() * PI) * 140).toInt()
                    canvas.drawColor((pulseAlpha shl 24) or 0x0000F0FF)
                }

                else -> {
                    // Smooth zoom/crossfade fallback for extended transitions
                    bitmapPaint.reset()
                    bitmapPaint.isFilterBitmap = true
                    bitmapPaint.colorFilter = getColorFilter(currentScene.filterEffect)
                    bitmapPaint.alpha = 255
                    drawFitted(canvas, currentBitmap, 0, 0, width, height, bitmapPaint)

                    bitmapPaint.colorFilter = getColorFilter(nextScene.filterEffect)
                    bitmapPaint.alpha = (t * 255).toInt()
                    canvas.save()
                    val sc = 1.08f - 0.08f * t
                    canvas.scale(sc, sc, width * 0.5f, height * 0.5f)
                    drawFitted(canvas, nextBitmap, 0, 0, width, height, bitmapPaint)
                    canvas.restore()
                }
            }
        }
    }

    /**
     * Renders Overlay / Picture-In-Picture layers onto canvas
     */
    fun renderOverlays(
        canvas: Canvas,
        overlays: List<OverlayImageItem>,
        overlayBitmaps: Map<String, Bitmap>,
        currentTimeSeconds: Double,
        width: Int,
        height: Int
    ) {
        val activeOverlays = overlays.filter {
            currentTimeSeconds >= it.startSeconds && currentTimeSeconds < it.startSeconds + it.durationSeconds
        }

        for (item in activeOverlays) {
            val bmp = overlayBitmaps[item.imagePath] ?: continue
            val cx = item.posX * width
            val cy = item.posY * height
            val targetW = (width * item.scale).toInt()
            val targetH = (bmp.height.toFloat() / bmp.width.toFloat() * targetW).toInt()

            canvas.save()
            canvas.translate(cx, cy)
            if (item.rotation != 0f) {
                canvas.rotate(item.rotation)
            }
            bitmapPaint.reset()
            bitmapPaint.isFilterBitmap = true
            bitmapPaint.alpha = (item.opacity.coerceIn(0f, 1f) * 255).toInt()
            dstRect.set(-targetW / 2, -targetH / 2, targetW / 2, targetH / 2)
            canvas.drawBitmap(bmp, null, dstRect, bitmapPaint)
            canvas.restore()
        }
    }

    /**
     * Renders ASS-style animated viral subtitles onto canvas
     */
    fun renderSubtitles(
        canvas: Canvas,
        subtitles: List<TextSyncItem>,
        currentTimeSeconds: Double,
        width: Int,
        height: Int,
        scaleFactor: Float = 1.0f
    ) {
        val activeSubtitles = subtitles.filter {
            currentTimeSeconds >= it.startSeconds && currentTimeSeconds <= it.endSeconds
        }

        for (item in activeSubtitles) {
            val elapsed = (currentTimeSeconds - item.startSeconds).toFloat()
            val duration = (item.endSeconds - item.startSeconds).toFloat()

            var scale = 1.0f
            var offsetY = 0f
            var offsetX = 0f
            var rotation = 0f
            var alpha = 1.0f
            var trackingExtra = 0f
            var textToDraw = item.text

            // Dynamic Motion presets
            when (item.animationPreset) {
                AnimationPreset.POP_UP -> {
                    if (elapsed < 0.12f) {
                        val p = elapsed / 0.12f
                        scale = 1.3f - 0.3f * p
                    }
                }
                AnimationPreset.BOUNCE -> {
                    if (elapsed < 0.22f) {
                        val p = elapsed / 0.22f
                        scale = (1.0f + 0.35f * sin(p * PI)).toFloat()
                    }
                }
                AnimationPreset.ZOOM_PUNCH -> {
                    if (elapsed < 0.14f) {
                        val p = elapsed / 0.14f
                        scale = 1.55f - 0.55f * p
                    }
                }
                AnimationPreset.KARAOKE_POP -> {
                    val pulse = (sin(elapsed * 9.0) * 0.18f).toFloat()
                    scale = 1.0f + pulse
                }
                AnimationPreset.ELASTIC_DROP -> {
                    if (elapsed < 0.28f) {
                        val p = elapsed / 0.28f
                        offsetY = -(1f - p) * 120f * scaleFactor
                        scale = 1.0f + (sin(p * PI * 2) * 0.2f * (1f - p)).toFloat()
                    }
                }
                AnimationPreset.HEARTBEAT_PULSE -> {
                    val p = (elapsed % 0.8f) / 0.8f
                    scale = 1.0f + (sin(p * PI * 2) * 0.15f * exp(-p * 3.0)).toFloat()
                }
                AnimationPreset.SLIDE_UP -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        offsetY = (1f - p) * 60f * scaleFactor
                        alpha = p
                    }
                }
                AnimationPreset.SLIDE_DOWN -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        offsetY = -(1f - p) * 60f * scaleFactor
                        alpha = p
                    }
                }
                AnimationPreset.SLIDE_LEFT -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        offsetX = (1f - p) * 90f * scaleFactor
                        alpha = p
                    }
                }
                AnimationPreset.SLIDE_RIGHT -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        offsetX = -(1f - p) * 90f * scaleFactor
                        alpha = p
                    }
                }
                AnimationPreset.RUBBER_BAND -> {
                    if (elapsed < 0.25f) {
                        val p = elapsed / 0.25f
                        scale = (1.0f + sin(p * PI * 3) * 0.28f * (1f - p)).toFloat()
                    }
                }
                AnimationPreset.FLIP_X -> {
                    if (elapsed < 0.25f) {
                        val p = elapsed / 0.25f
                        scale = cos(p * PI * 0.5).toFloat().coerceAtLeast(0.1f)
                    }
                }
                AnimationPreset.FLIP_Y -> {
                    if (elapsed < 0.25f) {
                        val p = elapsed / 0.25f
                        scale = sin(p * PI * 0.5).toFloat()
                    }
                }
                AnimationPreset.ROLL_IN -> {
                    if (elapsed < 0.25f) {
                        val p = elapsed / 0.25f
                        offsetX = -(1f - p) * 100f * scaleFactor
                        rotation = -(1f - p) * 45f
                    }
                }
                AnimationPreset.SKEW_SNAP -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        rotation = (sin(p * PI * 2) * 12f * (1f - p)).toFloat()
                    }
                }
                AnimationPreset.WORD_GLOW -> {
                    val pulse = (sin(elapsed * 7.5) * 0.12f).toFloat()
                    scale = 1.0f + pulse
                }
                AnimationPreset.SHAKE_IMPACT -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        offsetX = (sin(p * PI * 8) * 14f * (1f - p) * scaleFactor).toFloat()
                        offsetY = (cos(p * PI * 6) * 10f * (1f - p) * scaleFactor).toFloat()
                    }
                }
                AnimationPreset.WAVE_FLOAT -> {
                    offsetY = (sin(elapsed * 4.0) * 12f * scaleFactor).toFloat()
                }
                AnimationPreset.STROBE_FLASH -> {
                    alpha = if ((elapsed * 12).toInt() % 2 == 0) 1.0f else 0.4f
                }
                AnimationPreset.EXPLOSION -> {
                    if (elapsed < 0.18f) {
                        val p = elapsed / 0.18f
                        scale = 0.2f + p * 0.9f
                        rotation = (1f - p) * 25f
                    }
                }
                AnimationPreset.SPIRAL_IN -> {
                    if (elapsed < 0.25f) {
                        val p = elapsed / 0.25f
                        scale = p
                        rotation = (1f - p) * 180f
                    }
                }
                AnimationPreset.TYPEWRITER -> {
                    val chars = ((elapsed / 0.045f).toInt()).coerceIn(1, item.text.length)
                    textToDraw = item.text.take(chars)
                }
                AnimationPreset.GLITCH_FLICKER -> {
                    if (Math.random() < 0.2) {
                        offsetX = ((Math.random() - 0.5) * 12f * scaleFactor).toFloat()
                        offsetY = ((Math.random() - 0.5) * 8f * scaleFactor).toFloat()
                    }
                }
                AnimationPreset.WIGGLE -> {
                    rotation = (sin(elapsed * 15.0) * 5f).toFloat()
                }
                AnimationPreset.SMOKE_DISSOLVE -> {
                    if (duration - elapsed < 0.3f) {
                        val p = (duration - elapsed) / 0.3f
                        alpha = p.coerceIn(0f, 1f)
                        scale = 1f + (1f - p) * 0.3f
                    }
                }
                AnimationPreset.FADE_IN -> {
                    if (elapsed < 0.22f) {
                        alpha = (elapsed / 0.22f).coerceIn(0f, 1f)
                    }
                }
                AnimationPreset.ZOOM_OUT -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        scale = 0.5f + 0.5f * p
                    }
                }
                AnimationPreset.BLUR_IN -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        scale = 1.2f - 0.2f * p
                        alpha = p
                    }
                }
                AnimationPreset.EXPAND_TRACKING -> {
                    if (elapsed < 0.3f) {
                        val p = elapsed / 0.3f
                        trackingExtra = (1f - p) * 15f * scaleFactor
                    }
                }
                AnimationPreset.SWING_PENDULUM -> {
                    rotation = (sin(elapsed * 3.5) * 8f * exp(-elapsed * 0.5)).toFloat()
                }
                AnimationPreset.CAPCUT_FAST_BOUNCE -> {
                    if (elapsed < 0.2f) {
                        val p = elapsed / 0.2f
                        scale = (1.0f + 0.35f * sin(p * PI)).toFloat()
                    }
                }
                AnimationPreset.STOMP_SLAM -> {
                    if (elapsed < 0.15f) {
                        val p = elapsed / 0.15f
                        scale = 1.8f - 0.8f * p
                    }
                }
                AnimationPreset.RGB_SPLIT_MOTION -> {
                    if (elapsed < 0.25f) {
                        offsetX = (sin(elapsed * 25.0) * 8f * scaleFactor).toFloat()
                    }
                }
                else -> {
                    if (elapsed < 0.15f) {
                        val p = elapsed / 0.15f
                        scale = 0.85f + 0.15f * p
                        alpha = p
                    }
                }
            }

            val targetX = item.posX * width + offsetX
            val targetY = item.posY * height + offsetY
            val scaledFontSize = item.fontSize * scaleFactor

            // Parse colors
            val textColor = try { Color.parseColor(item.textColorHex) } catch (_: Exception) { Color.WHITE }
            val outlineColor = try { Color.parseColor(item.outlineColorHex) } catch (_: Exception) { Color.BLACK }
            val boxColor = item.boxColorHex?.let { try { Color.parseColor(it) } catch (_: Exception) { null } }

            val typeface = try {
                Typeface.create(item.fontName, Typeface.BOLD)
            } catch (_: Exception) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            textFillPaint.reset()
            textFillPaint.isAntiAlias = true
            textFillPaint.textAlign = Paint.Align.CENTER
            textFillPaint.textSize = scaledFontSize
            textFillPaint.color = textColor
            textFillPaint.typeface = typeface
            textFillPaint.alpha = (alpha * 255).toInt()

            textStrokePaint.reset()
            textStrokePaint.isAntiAlias = true
            textStrokePaint.textAlign = Paint.Align.CENTER
            textStrokePaint.style = Paint.Style.STROKE
            textStrokePaint.textSize = scaledFontSize
            textStrokePaint.color = outlineColor
            textStrokePaint.strokeWidth = scaledFontSize * 0.16f
            textStrokePaint.typeface = typeface
            textStrokePaint.alpha = (alpha * 255).toInt()

            canvas.save()
            canvas.translate(targetX, targetY)
            if (rotation != 0f) canvas.rotate(rotation)
            canvas.scale(scale, scale)

            val lines = textToDraw.split("\n")
            val lineHeight = scaledFontSize * 1.2f
            val totalTextHeight = lines.size * lineHeight
            val startY = -(totalTextHeight / 2f) + (scaledFontSize * 0.35f)

            // 1. Draw Rounded Box Background if Box effect
            if (item.textEffect == TextEffectPreset.BOX_BACKGROUND || boxColor != null) {
                val maxLineWidth = lines.maxOfOrNull { textFillPaint.measureText(it) } ?: (scaledFontSize * 4f)
                val padX = scaledFontSize * 0.5f
                val padY = scaledFontSize * 0.3f
                boxRect.set(
                    -maxLineWidth / 2f - padX,
                    -totalTextHeight / 2f - padY,
                    maxLineWidth / 2f + padX,
                    totalTextHeight / 2f + padY
                )
                boxPaint.reset()
                boxPaint.isAntiAlias = true
                boxPaint.color = boxColor ?: 0xCCFFE600.toInt()
                boxPaint.alpha = (alpha * 240).toInt()
                canvas.drawRoundRect(boxRect, scaledFontSize * 0.25f, scaledFontSize * 0.25f, boxPaint)
            }

            for ((lineIdx, line) in lines.withIndex()) {
                val currentLineY = startY + (lineIdx * lineHeight)

                // Multi-effect presets
                when (item.textEffect) {
                    TextEffectPreset.STROKE_3D, TextEffectPreset.RETRO_EXTRUDE -> {
                        textShadowPaint.reset()
                        textShadowPaint.isAntiAlias = true
                        textShadowPaint.textAlign = Paint.Align.CENTER
                        textShadowPaint.textSize = scaledFontSize
                        textShadowPaint.color = 0xFF000000.toInt()
                        textShadowPaint.typeface = typeface
                        textShadowPaint.alpha = (alpha * 220).toInt()
                        canvas.drawText(line, 5f * scaleFactor, currentLineY + 7f * scaleFactor, textShadowPaint)
                        canvas.drawText(line, 3f * scaleFactor, currentLineY + 4f * scaleFactor, textShadowPaint)
                    }
                    TextEffectPreset.GLOW -> {
                        textStrokePaint.strokeWidth = scaledFontSize * 0.28f
                        canvas.drawText(line, 0f, currentLineY, textStrokePaint)
                    }
                    TextEffectPreset.SHADOW -> {
                        textShadowPaint.reset()
                        textShadowPaint.isAntiAlias = true
                        textShadowPaint.textAlign = Paint.Align.CENTER
                        textShadowPaint.textSize = scaledFontSize
                        textShadowPaint.color = 0xAA000000.toInt()
                        textShadowPaint.typeface = typeface
                        canvas.drawText(line, 4f * scaleFactor, currentLineY + 5f * scaleFactor, textShadowPaint)
                    }
                    TextEffectPreset.DOUBLE_OUTLINE -> {
                        textStrokePaint.strokeWidth = scaledFontSize * 0.26f
                        textStrokePaint.color = Color.BLACK
                        canvas.drawText(line, 0f, currentLineY, textStrokePaint)
                        textStrokePaint.strokeWidth = scaledFontSize * 0.12f
                        textStrokePaint.color = outlineColor
                        canvas.drawText(line, 0f, currentLineY, textStrokePaint)
                    }
                    TextEffectPreset.FIRE_HEAT -> {
                        textShadowPaint.reset()
                        textShadowPaint.isAntiAlias = true
                        textShadowPaint.textAlign = Paint.Align.CENTER
                        textShadowPaint.textSize = scaledFontSize
                        textShadowPaint.color = 0xFFFF3300.toInt()
                        textShadowPaint.typeface = typeface
                        canvas.drawText(line, 0f, currentLineY - 3f * scaleFactor, textShadowPaint)
                    }
                    TextEffectPreset.ICE_FROST -> {
                        textStrokePaint.strokeWidth = scaledFontSize * 0.24f
                        textStrokePaint.color = 0xFF00E5FF.toInt()
                        canvas.drawText(line, 0f, currentLineY, textStrokePaint)
                    }
                    TextEffectPreset.METALLIC_CHROME, TextEffectPreset.GRADIENT_SUNSET -> {
                        val textWidth = textFillPaint.measureText(line)
                        textFillPaint.shader = LinearGradient(
                            -textWidth / 2f, currentLineY - scaledFontSize,
                            textWidth / 2f, currentLineY,
                            intArrayOf(textColor, Color.WHITE, textColor),
                            null,
                            Shader.TileMode.CLAMP
                        )
                    }
                    else -> {}
                }

                // Draw stroke outline
                if (item.textEffect != TextEffectPreset.BOX_BACKGROUND) {
                    canvas.drawText(line, 0f, currentLineY, textStrokePaint)
                }

                // Draw text fill
                canvas.drawText(line, 0f, currentLineY, textFillPaint)
                textFillPaint.shader = null
            }

            canvas.restore()
        }
    }
}
