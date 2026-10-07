package com.example.util

import com.example.model.AnimationPreset
import com.example.model.ProjectAspectRatio
import com.example.model.TextEffectPreset
import com.example.model.TextSyncItem
import java.util.Locale

object AssSubtitleGenerator {

    /**
     * Converts hex color string (#RRGGBB or #AARRGGBB) to ASS color format (&HAABBGGRR)
     */
    fun hexToAssColor(hex: String, alphaHex: String = "00"): String {
        val clean = hex.removePrefix("#").trim()
        val (r, g, b) = when (clean.length) {
            6 -> Triple(clean.substring(0, 2), clean.substring(2, 4), clean.substring(4, 6))
            8 -> Triple(clean.substring(2, 4), clean.substring(4, 6), clean.substring(6, 8))
            3 -> Triple(clean[0].toString() + clean[0], clean[1].toString() + clean[1], clean[2].toString() + clean[2])
            else -> Triple("FF", "FF", "FF")
        }
        // In ASS format: &HAABBGGRR
        return "&H$alphaHex$b$g$r"
    }

    /**
     * Converts seconds to ASS time format: H:MM:SS.cs (e.g., 0:00:03.20)
     */
    fun formatAssTime(seconds: Double): String {
        val totalCentis = (seconds * 100).toLong()
        val hours = totalCentis / 360000
        val mins = (totalCentis % 360000) / 6000
        val secs = (totalCentis % 6000) / 100
        val centis = totalCentis % 100
        return String.format(Locale.US, "%d:%02d:%02d.%02d", hours, mins, secs, centis)
    }

    /**
     * Generates a complete .ass subtitle script from items
     */
    fun generateAssScript(
        items: List<TextSyncItem>,
        aspectRatio: ProjectAspectRatio = ProjectAspectRatio.SHORTS_9_16
    ): String {
        val playResX = aspectRatio.width
        val playResY = aspectRatio.height

        val sb = StringBuilder()
        sb.appendLine("[Script Info]")
        sb.appendLine("Title: SyncCut Studio Viral Subtitles")
        sb.appendLine("ScriptType: v4.00+")
        sb.appendLine("WrapStyle: 0")
        sb.appendLine("ScaledBorderAndShadow: yes")
        sb.appendLine("YCbCr Matrix: TV.709")
        sb.appendLine("PlayResX: $playResX")
        sb.appendLine("PlayResY: $playResY")
        sb.appendLine()

        sb.appendLine("[V4+ Styles]")
        sb.appendLine("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding")

        // Default base style
        sb.appendLine("Style: Default,Anton,68,&H0000E6FF,&H000000FF,&H00000000,&H80000000,-1,0,0,0,100,100,1,0,1,5,3,2,40,40,220,1")
        sb.appendLine()

        sb.appendLine("[Events]")
        sb.appendLine("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text")

        for (item in items) {
            val startTime = formatAssTime(item.startSeconds)
            val endTime = formatAssTime(item.endSeconds)

            val primaryColor = hexToAssColor(item.textColorHex)
            val outlineColor = hexToAssColor(item.outlineColorHex)

            // Calculate screen coordinate overrides based on item.posX and item.posY
            val targetX = (item.posX * playResX).toInt()
            val targetY = (item.posY * playResY).toInt()

            // Animation override tags
            val motionTag = when (item.animationPreset) {
                AnimationPreset.POP_UP -> "{\\fscx120\\fscy120\\t(0,140,\\fscx100\\fscy100)}"
                AnimationPreset.BOUNCE -> "{\\fscx85\\fscy125\\t(0,90,\\fscx115\\fscy90)\\t(90,180,\\fscx100\\fscy100)}"
                AnimationPreset.WORD_GLOW -> "{\\blur5\\t(0,160,\\blur1)}"
                AnimationPreset.TYPEWRITER -> "{\\fad(60,60)}"
                AnimationPreset.SLIDE_UP -> "{\\move($targetX,${targetY + 40},$targetX,$targetY,0,160)}"
                AnimationPreset.FADE_IN -> "{\\fad(180,120)}"
                AnimationPreset.ZOOM_PUNCH -> "{\\fscx135\\fscy135\\t(0,120,\\fscx100\\fscy100)}"
                AnimationPreset.CAPCUT_FAST_BOUNCE -> "{\\fscx130\\fscy130\\t(0,100,\\fscx95\\fscy95)\\t(100,180,\\fscx100\\fscy100)}"
                AnimationPreset.STOMP_SLAM -> "{\\fscx180\\fscy180\\t(0,120,\\fscx100\\fscy100)}"
                AnimationPreset.SLIDE_DOWN -> "{\\move($targetX,${targetY - 40},$targetX,$targetY,0,160)}"
                AnimationPreset.SLIDE_LEFT -> "{\\move(${targetX + 60},$targetY,$targetX,$targetY,0,160)}"
                AnimationPreset.SLIDE_RIGHT -> "{\\move(${targetX - 60},$targetY,$targetX,$targetY,0,160)}"
                else -> "{\\fad(80,80)}"
            }

            val effectTag = when (item.textEffect) {
                TextEffectPreset.GLOW -> "\\blur4\\bord4"
                TextEffectPreset.STROKE_3D -> "\\bord6\\shad5"
                TextEffectPreset.SHADOW -> "\\bord2\\shad4"
                TextEffectPreset.NORMAL -> "\\bord3\\shad1"
                TextEffectPreset.DOUBLE_OUTLINE -> "\\bord5\\shad3"
                TextEffectPreset.METALLIC_CHROME, TextEffectPreset.GOLD_METALLIC -> "\\bord4\\shad3"
                TextEffectPreset.FIRE_HEAT, TextEffectPreset.ICE_FROST -> "\\blur6\\bord4"
                else -> "\\bord3\\shad2"
            }

            // Position tag: \an5 is center alignment at (posX, posY)
            val posTag = if (item.animationPreset != AnimationPreset.SLIDE_UP) {
                "\\pos($targetX,$targetY)"
            } else ""

            val fontTag = "\\fn${item.fontName}\\fs${(item.fontSize * 1.6).toInt()}\\1c$primaryColor\\3c$outlineColor"

            val fullText = "{$posTag\\an5$fontTag$effectTag}$motionTag${item.text}"
            sb.appendLine("Dialogue: 0,$startTime,$endTime,Default,,0,0,0,,${fullText.replace("\n", "\\N")}")
        }

        return sb.toString()
    }
}
