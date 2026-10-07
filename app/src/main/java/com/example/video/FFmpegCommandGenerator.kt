package com.example.video

import com.example.model.ProjectModel
import com.example.model.TimelineItemModel
import com.example.model.TrackType
import com.example.model.VideoProject
import java.io.File
import java.util.Locale

/**
 * Generates dynamic FFmpeg CLI commands with complex filtergraphs (-filter_complex)
 * for multi-layer video synthesis (media scaling, overlays, drawtext captions, transitions, audio mixing).
 */
object FFmpegCommandGenerator {

    data class FFmpegScriptResult(
        val commandLine: String,
        val filterComplex: String,
        val inputArguments: List<String>,
        val outputArguments: List<String>
    )

    fun generateFilterComplexScript(
        project: VideoProject,
        outputFile: File,
        width: Int = 1080,
        height: Int = 1920,
        fps: Int = 30
    ): FFmpegScriptResult {
        val inputs = mutableListOf<String>()
        val filterParts = mutableListOf<String>()
        var inputIndex = 0

        val mainImages = project.images
        val overlays = project.overlays
        val subtitles = project.subtitles
        val audios = project.audioTracks

        // 1. Inputs for Main Images
        val mainLabels = mutableListOf<String>()
        mainImages.forEachIndexed { idx, img ->
            inputs.add("-loop")
            inputs.add("1")
            inputs.add("-t")
            inputs.add(String.format(Locale.US, "%.3f", img.durationSeconds))
            inputs.add("-i")
            inputs.add(img.imagePath.ifEmpty { "scene_${idx + 1}.jpg" })

            val label = "v_main_$idx"
            filterParts.add("[$inputIndex:v]scale=$width:$height:force_original_aspect_ratio=increase,crop=$width:$height,setsar=1,fps=$fps,setpts=PTS-STARTPTS[$label]")
            mainLabels.add(label)
            inputIndex++
        }

        // Concat main video segments
        var currentVideoLabel = if (mainLabels.isNotEmpty()) {
            if (mainLabels.size == 1) {
                mainLabels[0]
            } else {
                val concatInputs = mainLabels.joinToString("") { "[$it]" }
                val concatLabel = "v_concat"
                filterParts.add("$concatInputs concat=n=${mainLabels.size}:v=1:a=0[$concatLabel]")
                concatLabel
            }
        } else {
            // Color base
            val colorLabel = "v_base"
            filterParts.add("color=c=black:s=${width}x${height}:d=10:r=$fps[$colorLabel]")
            colorLabel
        }

        // 2. Overlays
        overlays.forEachIndexed { idx, ov ->
            inputs.add("-loop")
            inputs.add("1")
            inputs.add("-t")
            inputs.add(String.format(Locale.US, "%.3f", ov.durationSeconds))
            inputs.add("-i")
            inputs.add(ov.imagePath)

            val ovInput = inputIndex
            inputIndex++

            val ovScaled = "ov_scaled_$idx"
            val ovW = (width * ov.scale).toInt()
            val ovH = (height * ov.scale).toInt()
            filterParts.add("[$ovInput:v]scale=$ovW:$ovH:force_original_aspect_ratio=increase,format=rgba[$ovScaled]")

            val nextVideoLabel = "v_overlay_$idx"
            val posXExpr = "(W-w)*${String.format(Locale.US, "%.2f", ov.posX)}"
            val posYExpr = "(H-h)*${String.format(Locale.US, "%.2f", ov.posY)}"
            val enableExpr = "between(t,${String.format(Locale.US, "%.2f", ov.startSeconds)},${String.format(Locale.US, "%.2f", ov.startSeconds + ov.durationSeconds)})"

            filterParts.add("[$currentVideoLabel][$ovScaled]overlay=x=$posXExpr:y=$posYExpr:enable='$enableExpr':format=auto[$nextVideoLabel]")
            currentVideoLabel = nextVideoLabel
        }

        // 3. Subtitles / Drawtext
        subtitles.forEachIndexed { idx, sub ->
            val nextVideoLabel = "v_txt_$idx"
            val start = String.format(Locale.US, "%.2f", sub.startSeconds)
            val end = String.format(Locale.US, "%.2f", sub.endSeconds)
            val posX = "(w-text_w)*${String.format(Locale.US, "%.2f", sub.posX)}"
            val posY = "(h-text_h)*${String.format(Locale.US, "%.2f", sub.posY)}"
            val escapedText = sub.text.replace("'", "\\'").replace(":", "\\:")
            val fontSize = (sub.fontSize * 2.2).toInt().coerceIn(24, 96)

            filterParts.add("[$currentVideoLabel]drawtext=text='$escapedText':fontsize=$fontSize:fontcolor=white:borderw=4:bordercolor=black:x=$posX:y=$posY:enable='between(t,$start,$end)'[$nextVideoLabel]")
            currentVideoLabel = nextVideoLabel
        }

        // 4. Audio Tracks
        val audioLabels = mutableListOf<String>()
        audios.forEachIndexed { idx, aud ->
            inputs.add("-i")
            inputs.add(aud.audioPath)
            val audInput = inputIndex
            inputIndex++

            val audLabel = "a_mix_$idx"
            filterParts.add("[$audInput:a]volume=${String.format(Locale.US, "%.2f", aud.volume)}[$audLabel]")
            audioLabels.add(audLabel)
        }

        val hasAudio = audioLabels.isNotEmpty()
        val currentAudioLabel = if (hasAudio) {
            if (audioLabels.size == 1) {
                audioLabels[0]
            } else {
                val mixInputs = audioLabels.joinToString("") { "[$it]" }
                val mixedLabel = "a_final"
                filterParts.add("$mixInputs amix=inputs=${audioLabels.size}:duration=first:dropout_transition=2[$mixedLabel]")
                mixedLabel
            }
        } else {
            null
        }

        val filterComplexStr = filterParts.joinToString("; ")

        val outputArgs = mutableListOf<String>()
        outputArgs.add("-map")
        outputArgs.add("[$currentVideoLabel]")
        if (currentAudioLabel != null) {
            outputArgs.add("-map")
            outputArgs.add("[$currentAudioLabel]")
        }
        outputArgs.add("-c:v")
        outputArgs.add("libx264")
        outputArgs.add("-preset")
        outputArgs.add("fast")
        outputArgs.add("-crf")
        outputArgs.add("20")
        outputArgs.add("-pix_fmt")
        outputArgs.add("yuv420p")
        if (currentAudioLabel != null) {
            outputArgs.add("-c:a")
            outputArgs.add("aac")
            outputArgs.add("-b:a")
            outputArgs.add("192k")
        }
        outputArgs.add("-shortest")
        outputArgs.add("-y")
        outputArgs.add(outputFile.absolutePath)

        val fullCmd = "ffmpeg " + inputs.joinToString(" ") + " -filter_complex \"" + filterComplexStr + "\" " + outputArgs.joinToString(" ")

        return FFmpegScriptResult(
            commandLine = fullCmd,
            filterComplex = filterComplexStr,
            inputArguments = inputs,
            outputArguments = outputArgs
        )
    }
}
