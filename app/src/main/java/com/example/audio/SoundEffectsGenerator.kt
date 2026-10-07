package com.example.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class BuiltinAudioPreset(
    val id: String,
    val title: String,
    val category: String,
    val durationSeconds: Double,
    val isBgm: Boolean
)

object SoundEffectsGenerator {

    val BUILTIN_AUDIO_LIST = listOf(
        // Sound Effects (SFX)
        BuiltinAudioPreset("sfx_whoosh", "Viral Whoosh Transition", "SFX", 0.6, false),
        BuiltinAudioPreset("sfx_pop", "Hormozi Pop Hook", "SFX", 0.4, false),
        BuiltinAudioPreset("sfx_boom", "Cinematic Sub Boom", "SFX", 1.8, false),
        BuiltinAudioPreset("sfx_bell", "Ding Notification", "SFX", 0.8, false),
        BuiltinAudioPreset("sfx_camera", "Camera Shutter Click", "SFX", 0.5, false),
        BuiltinAudioPreset("sfx_riser", "Suspense Tension Riser", "SFX", 2.0, false),
        BuiltinAudioPreset("sfx_glitch", "Cyber Glitch Scratch", "SFX", 0.7, false),
        BuiltinAudioPreset("sfx_cash", "Cash Register Cha-Ching", "SFX", 1.0, false),
        BuiltinAudioPreset("sfx_applause", "Audience Crowd Clap", "SFX", 2.5, false),
        BuiltinAudioPreset("sfx_typing", "Mechanical Keyboard Type", "SFX", 1.2, false),
        BuiltinAudioPreset("sfx_impact", "Heavy Cinematic Hit", "SFX", 1.5, false),
        // Background Music (BGM)
        BuiltinAudioPreset("bgm_lofi", "Lofi Chill Sunset (Loop)", "BGM", 8.0, true),
        BuiltinAudioPreset("bgm_upbeat", "Upbeat Viral Shorts Beat (Loop)", "BGM", 6.0, true),
        BuiltinAudioPreset("bgm_cyber", "Cyber Synthwave Drive (Loop)", "BGM", 7.0, true),
        BuiltinAudioPreset("bgm_ambient", "Deep Ambient Cinematic (Loop)", "BGM", 9.0, true)
    )

    /**
     * Ensures all built-in audio WAV files are generated and returns file reference
     */
    fun getAudioFile(context: Context, presetId: String): File {
        val audioDir = File(context.filesDir, "builtin_audio").apply { mkdirs() }
        val targetFile = File(audioDir, "$presetId.wav")
        if (!targetFile.exists() || targetFile.length() < 100) {
            generateWav(targetFile, presetId)
        }
        return targetFile
    }

    private fun generateWav(file: File, id: String) {
        val sampleRate = 44100
        val (numSamples, generator) = when (id) {
            "sfx_whoosh" -> {
                val duration = 0.6
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val freq = 200.0 + 900.0 * sin(t / 0.6 * PI)
                    val envelope = sin(t / 0.6 * PI)
                    val noise = (Math.random() * 2.0 - 1.0) * 0.4
                    (sin(2.0 * PI * freq * t) * 0.6 + noise) * envelope
                }
            }
            "sfx_pop" -> {
                val duration = 0.4
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val freq = 450.0 + 800.0 * exp(-t * 25.0)
                    val envelope = exp(-t * 18.0)
                    sin(2.0 * PI * freq * t) * envelope
                }
            }
            "sfx_boom" -> {
                val duration = 1.8
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val freq = 55.0 + 60.0 * exp(-t * 3.0)
                    val envelope = exp(-t * 2.2)
                    sin(2.0 * PI * freq * t) * envelope
                }
            }
            "sfx_bell" -> {
                val duration = 0.8
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val s1 = sin(2.0 * PI * 1046.5 * t)
                    val s2 = sin(2.0 * PI * 2093.0 * t) * 0.5
                    val envelope = exp(-t * 5.0)
                    (s1 + s2) * envelope
                }
            }
            "sfx_camera" -> {
                val duration = 0.5
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val click = if (t < 0.05 || (t > 0.12 && t < 0.17)) (Math.random() * 2.0 - 1.0) else 0.0
                    click * exp(-t * 12.0)
                }
            }
            "sfx_riser" -> {
                val duration = 2.0
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val freq = 120.0 + 900.0 * (t / 2.0) * (t / 2.0)
                    val envelope = (t / 2.0)
                    (sin(2.0 * PI * freq * t) + (Math.random() * 2.0 - 1.0) * 0.2) * envelope
                }
            }
            "sfx_glitch" -> {
                val duration = 0.7
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val noise = (Math.random() * 2.0 - 1.0)
                    val buzz = sin(2.0 * PI * 80.0 * t)
                    val square = if (sin(2.0 * PI * 440.0 * t) > 0) 0.5 else -0.5
                    (noise * 0.5 + buzz * 0.3 + square * 0.2) * exp(-t * 4.0)
                }
            }
            "sfx_cash" -> {
                val duration = 1.0
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val bell = sin(2.0 * PI * 2400.0 * t) * exp(-t * 4.0)
                    val click = if (t < 0.08) (Math.random() * 2.0 - 1.0) * 0.7 else 0.0
                    bell + click
                }
            }
            "sfx_applause" -> {
                val duration = 2.5
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val envelope = sin(t / 2.5 * PI)
                    val noise = (Math.random() * 2.0 - 1.0)
                    noise * envelope * 0.6
                }
            }
            "sfx_typing" -> {
                val duration = 1.2
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val clickInterval = (t * 8.0) % 1.0
                    val click = if (clickInterval < 0.08) (Math.random() * 2.0 - 1.0) else 0.0
                    click * 0.8
                }
            }
            "sfx_impact" -> {
                val duration = 1.5
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val bass = sin(2.0 * PI * (60.0 * exp(-t * 4.0)) * t) * exp(-t * 2.5)
                    val noise = if (t < 0.1) (Math.random() * 2.0 - 1.0) * exp(-t * 15.0) else 0.0
                    bass * 0.8 + noise * 0.4
                }
            }
            "bgm_lofi" -> {
                val duration = 8.0
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val root = when (((t / 2.0).toInt()) % 4) {
                        0 -> 220.0 // A3
                        1 -> 174.61 // F3
                        2 -> 130.81 // C3
                        else -> 196.0 // G3
                    }
                    val bass = sin(2.0 * PI * (root / 2.0) * t) * 0.4
                    val chord = (sin(2.0 * PI * root * t) + sin(2.0 * PI * (root * 1.25) * t) + sin(2.0 * PI * (root * 1.5) * t)) * 0.15
                    val kickBeat = (t * 2.0) % 1.0
                    val kick = if (kickBeat < 0.1) sin(2.0 * PI * (80.0 * (1.0 - kickBeat * 10.0)) * t) * 0.4 else 0.0
                    (bass + chord + kick)
                }
            }
            "bgm_cyber" -> {
                val duration = 7.0
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val step = ((t * 4.0).toInt()) % 8
                    val note = when (step) {
                        0 -> 146.83 // D3
                        1 -> 146.83
                        2 -> 174.61 // F3
                        3 -> 220.00 // A3
                        4 -> 196.00 // G3
                        5 -> 174.61
                        6 -> 164.81 // E3
                        else -> 146.83
                    }
                    val synth = sin(2.0 * PI * note * t) * 0.35
                    val kick = if ((t * 2.0) % 1.0 < 0.1) sin(2.0 * PI * 85.0 * t) * 0.5 else 0.0
                    synth + kick
                }
            }
            "bgm_ambient" -> {
                val duration = 9.0
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val drone1 = sin(2.0 * PI * 110.0 * t) * 0.25
                    val drone2 = sin(2.0 * PI * 164.81 * t) * 0.2
                    val shimmer = sin(2.0 * PI * 440.0 * t) * (sin(t * 0.5) * 0.15)
                    drone1 + drone2 + shimmer
                }
            }
            else -> { // bgm_upbeat
                val duration = 6.0
                val total = (sampleRate * duration).toInt()
                Pair(total) { i: Int ->
                    val t = i.toDouble() / sampleRate
                    val beat = (t * 2.2) % 1.0
                    val freq = when (((t * 2.2).toInt()) % 4) {
                        0 -> 261.63 // C4
                        1 -> 329.63 // E4
                        2 -> 392.00 // G4
                        else -> 523.25 // C5
                    }
                    val pluck = sin(2.0 * PI * freq * t) * exp(-beat * 4.0) * 0.35
                    val kick = if (beat < 0.12) sin(2.0 * PI * (90.0 * (1.0 - beat * 8.0)) * t) * 0.5 else 0.0
                    pluck + kick
                }
            }
        }

        try {
            FileOutputStream(file).use { fos ->
                writeWavHeader(fos, sampleRate, numSamples)
                val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until numSamples) {
                    val sample = (generator(i).coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                    buffer.putShort(sample)
                }
                fos.write(buffer.array())
            }
        } catch (_: Exception) {}
    }

    private fun writeWavHeader(out: FileOutputStream, sampleRate: Int, numSamples: Int) {
        val totalDataLen = numSamples * 2 + 36
        val byteRate = sampleRate * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = 1; header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0
        header[34] = 16; header[35] = 0

        val dataSize = numSamples * 2
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }
}
