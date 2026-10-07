package com.example.util

import com.example.model.AnimationPreset
import com.example.model.FilterEffectType
import com.example.model.FramingMode
import com.example.model.ImageSyncItem
import com.example.model.TextEffectPreset
import com.example.model.TextSyncItem
import com.example.model.TransitionType
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

object DualJsonParser {

    /**
     * Parses time string (e.g., "00:01:23.450", "01:23.5", "83.5") or numeric representation into seconds.
     */
    fun parseTimeToSeconds(timeValue: Any?): Double {
        if (timeValue == null) return 0.0
        if (timeValue is Number) return timeValue.toDouble()

        val str = timeValue.toString().trim()
        if (str.isEmpty()) return 0.0

        // If direct double
        str.toDoubleOrNull()?.let { return it }

        // Format: HH:MM:SS.cs or MM:SS.cs or MM:SS
        val parts = str.split(":")
        return try {
            when (parts.size) {
                3 -> {
                    val hours = parts[0].trim().toDoubleOrNull() ?: 0.0
                    val minutes = parts[1].trim().toDoubleOrNull() ?: 0.0
                    val seconds = parts[2].trim().toDoubleOrNull() ?: 0.0
                    hours * 3600.0 + minutes * 60.0 + seconds
                }
                2 -> {
                    val minutes = parts[0].trim().toDoubleOrNull() ?: 0.0
                    val seconds = parts[1].trim().toDoubleOrNull() ?: 0.0
                    minutes * 60.0 + seconds
                }
                1 -> parts[0].trim().toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        } catch (_: Exception) {
            0.0
        }
    }

    /**
     * Formats seconds into HH:MM:SS.cs string
     */
    fun formatSecondsToTimecode(seconds: Double): String {
        val totalMillis = (seconds * 1000).toLong()
        val hours = totalMillis / 3600000
        val minutes = (totalMillis % 3600000) / 60000
        val secs = (totalMillis % 60000) / 1000
        val centis = (totalMillis % 1000) / 10
        return String.format(Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, secs, centis)
    }

    /**
     * Parses image_sync.json content into a list of ImageSyncItems
     */
    fun parseImageSyncJson(jsonString: String): List<ImageSyncItem> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()

        val jsonArray: JSONArray = try {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val obj = JSONObject(trimmed)
                when {
                    obj.has("images") -> obj.getJSONArray("images")
                    obj.has("scenes") -> obj.getJSONArray("scenes")
                    obj.has("clips") -> obj.getJSONArray("clips")
                    else -> JSONArray()
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        val items = mutableListOf<ImageSyncItem>()
        for (i in 0 until jsonArray.length()) {
            val itemObj = jsonArray.optJSONObject(i) ?: continue

            val id = itemObj.optString("id").ifEmpty { "scene_${i + 1}" }
            val path = when {
                itemObj.has("imagePath") -> itemObj.optString("imagePath")
                itemObj.has("imageUri") -> itemObj.optString("imageUri")
                itemObj.has("path") -> itemObj.optString("path")
                itemObj.has("file") -> itemObj.optString("file")
                else -> "scene_${i + 1}.jpg"
            }

            val duration = itemObj.optDouble("durationSeconds", itemObj.optDouble("duration", 3.0)).let {
                if (it <= 0.0) 3.0 else it
            }

            val transitionStr = itemObj.optString("transition", "CROSSFADE")
            val transition = try {
                TransitionType.valueOf(transitionStr.uppercase(Locale.US))
            } catch (_: Exception) {
                TransitionType.entries.find { it.name.equals(transitionStr, ignoreCase = true) || it.label.equals(transitionStr, ignoreCase = true) } ?: TransitionType.CROSSFADE
            }

            val filterStr = itemObj.optString("filterEffect", itemObj.optString("filter", "NORMAL"))
            val filter = try {
                FilterEffectType.valueOf(filterStr.uppercase(Locale.US))
            } catch (_: Exception) {
                FilterEffectType.entries.find { it.name.equals(filterStr, ignoreCase = true) || it.label.equals(filterStr, ignoreCase = true) } ?: FilterEffectType.NORMAL
            }

            val framingStr = itemObj.optString("framingMode", "CROP_TO_FILL")
            val framing = try {
                FramingMode.valueOf(framingStr.uppercase(Locale.US))
            } catch (_: Exception) {
                FramingMode.CROP_TO_FILL
            }

            items.add(
                ImageSyncItem(
                    id = id,
                    imagePath = path,
                    durationSeconds = duration,
                    transition = transition,
                    filterEffect = filter,
                    framingMode = framing
                )
            )
        }
        return items
    }

    /**
     * Serializes ImageSyncItems into pretty-printed JSON
     */
    fun serializeImageSyncJson(items: List<ImageSyncItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("imagePath", item.imagePath)
            obj.put("durationSeconds", item.durationSeconds)
            obj.put("transition", item.transition.name)
            obj.put("filterEffect", item.filterEffect.name)
            obj.put("framingMode", item.framingMode.name)
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Parses text_sync.json content into a list of TextSyncItems
     * Handles flexible formats, missing timestamps, and auto-spacing
     */
    fun parseTextSyncJson(jsonString: String, totalVideoDuration: Double = 15.0): List<TextSyncItem> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()

        val jsonArray: JSONArray = try {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val obj = JSONObject(trimmed)
                when {
                    obj.has("subtitles") -> obj.getJSONArray("subtitles")
                    obj.has("captions") -> obj.getJSONArray("captions")
                    obj.has("texts") -> obj.getJSONArray("texts")
                    obj.has("dialogue") -> obj.getJSONArray("dialogue")
                    else -> JSONArray()
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        val rawList = mutableListOf<TextSyncItem>()
        var previousEnd = 0.0

        for (i in 0 until jsonArray.length()) {
            val itemObj = jsonArray.optJSONObject(i) ?: continue

            val id = itemObj.optString("id").ifEmpty { "sub_${i + 1}" }
            val text = when {
                itemObj.has("text") -> itemObj.optString("text")
                itemObj.has("caption") -> itemObj.optString("caption")
                itemObj.has("content") -> itemObj.optString("content")
                else -> ""
            }

            // Start & End can be numbers or "00:00:02.50" strings
            val rawStart = itemObj.opt("startSeconds") ?: itemObj.opt("start") ?: itemObj.opt("startTime")
            val rawEnd = itemObj.opt("endSeconds") ?: itemObj.opt("end") ?: itemObj.opt("endTime")

            var start = parseTimeToSeconds(rawStart)
            var end = parseTimeToSeconds(rawEnd)

            // Auto-space if missing or invalid
            if (rawStart == null && rawEnd == null) {
                start = previousEnd
                val wordCount = maxOf(1, text.split("\\s+".toRegex()).size)
                val duration = maxOf(1.2, wordCount * 0.38)
                end = start + duration
            } else if (end <= start) {
                end = start + 2.0
            }
            previousEnd = end

            val animStr = itemObj.optString("animationPreset", itemObj.optString("animation", "POP_UP"))
            val anim = try {
                AnimationPreset.valueOf(animStr.uppercase(Locale.US))
            } catch (_: Exception) {
                AnimationPreset.entries.find { it.name.equals(animStr, ignoreCase = true) || it.label.contains(animStr, ignoreCase = true) } ?: AnimationPreset.POP_UP
            }

            val effectStr = itemObj.optString("textEffect", itemObj.optString("effect", "STROKE_3D"))
            val effect = try {
                TextEffectPreset.valueOf(effectStr.uppercase(Locale.US))
            } catch (_: Exception) {
                TextEffectPreset.STROKE_3D
            }

            val fontName = itemObj.optString("fontName", itemObj.optString("font", "Anton"))
            val fontSize = itemObj.optInt("fontSize", 40)
            val textColor = itemObj.optString("textColorHex", itemObj.optString("textColor", "#FFE600"))
            val outlineColor = itemObj.optString("outlineColorHex", itemObj.optString("outlineColor", "#000000"))
            val posX = itemObj.optDouble("posX", 0.5).toFloat()
            val posY = itemObj.optDouble("posY", 0.78).toFloat()

            rawList.add(
                TextSyncItem(
                    id = id,
                    text = text,
                    startSeconds = start,
                    endSeconds = end,
                    animationPreset = anim,
                    textEffect = effect,
                    fontName = fontName,
                    fontSize = fontSize,
                    textColorHex = textColor,
                    outlineColorHex = outlineColor,
                    posX = posX,
                    posY = posY
                )
            )
        }
        return rawList
    }

    /**
     * Serializes TextSyncItems into pretty-printed JSON
     */
    fun serializeTextSyncJson(items: List<TextSyncItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("startSeconds", item.startSeconds)
            obj.put("endSeconds", item.endSeconds)
            obj.put("startTimecode", formatSecondsToTimecode(item.startSeconds))
            obj.put("endTimecode", formatSecondsToTimecode(item.endSeconds))
            obj.put("animationPreset", item.animationPreset.name)
            obj.put("textEffect", item.textEffect.name)
            obj.put("fontName", item.fontName)
            obj.put("fontSize", item.fontSize)
            obj.put("textColorHex", item.textColorHex)
            obj.put("outlineColorHex", item.outlineColorHex)
            if (item.boxColorHex != null) obj.put("boxColorHex", item.boxColorHex)
            obj.put("posX", item.posX)
            obj.put("posY", item.posY)
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Parses audio_sync.json into List<AudioTrackItem>
     */
    fun parseAudioSyncJson(jsonString: String): List<com.example.model.AudioTrackItem> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.model.AudioTrackItem>()
        try {
            val array = JSONArray(trimmed)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.model.AudioTrackItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        audioPath = obj.optString("audioPath", ""),
                        title = obj.optString("title", "Audio Clip"),
                        startSeconds = obj.optDouble("startSeconds", 0.0),
                        durationSeconds = obj.optDouble("durationSeconds", 5.0),
                        audioOffsetSeconds = obj.optDouble("audioOffsetSeconds", 0.0),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        isMuted = obj.optBoolean("isMuted", false),
                        isBgm = obj.optBoolean("isBgm", true)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeAudioSyncJson(items: List<com.example.model.AudioTrackItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("audioPath", item.audioPath)
            obj.put("title", item.title)
            obj.put("startSeconds", item.startSeconds)
            obj.put("durationSeconds", item.durationSeconds)
            obj.put("audioOffsetSeconds", item.audioOffsetSeconds)
            obj.put("volume", item.volume)
            obj.put("isMuted", item.isMuted)
            obj.put("isBgm", item.isBgm)
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Parses overlay_sync.json into List<OverlayImageItem>
     */
    fun parseOverlaySyncJson(jsonString: String): List<com.example.model.OverlayImageItem> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.model.OverlayImageItem>()
        try {
            val array = JSONArray(trimmed)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.model.OverlayImageItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        imagePath = obj.optString("imagePath", ""),
                        startSeconds = obj.optDouble("startSeconds", 0.0),
                        durationSeconds = obj.optDouble("durationSeconds", 3.0),
                        posX = obj.optDouble("posX", 0.5).toFloat(),
                        posY = obj.optDouble("posY", 0.5).toFloat(),
                        scale = obj.optDouble("scale", 0.45).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        opacity = obj.optDouble("opacity", 1.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeOverlaySyncJson(items: List<com.example.model.OverlayImageItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("imagePath", item.imagePath)
            obj.put("startSeconds", item.startSeconds)
            obj.put("durationSeconds", item.durationSeconds)
            obj.put("posX", item.posX)
            obj.put("posY", item.posY)
            obj.put("scale", item.scale)
            obj.put("rotation", item.rotation)
            obj.put("opacity", item.opacity)
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Parses raw JSON automation script into a list of TimelineItemModels.
     * Supports multi-layer tracks: mainMedia, audio, text, overlay.
     */
    fun parseJsonAutomationScript(rawJson: String): List<com.example.model.TimelineItemModel> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()

        val jsonArray = try {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val obj = JSONObject(trimmed)
                when {
                    obj.has("items") -> obj.getJSONArray("items")
                    obj.has("layers") -> obj.getJSONArray("layers")
                    obj.has("timeline") -> obj.getJSONArray("timeline")
                    obj.has("tracks") -> obj.getJSONArray("tracks")
                    else -> JSONArray()
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        val result = mutableListOf<com.example.model.TimelineItemModel>()
        for (i in 0 until jsonArray.length()) {
            val itemObj = jsonArray.optJSONObject(i) ?: continue

            val typeStr = itemObj.optString("track_type", itemObj.optString("type", "mainMedia")).lowercase(Locale.US)
            val trackType = when {
                typeStr.contains("audio") || typeStr.contains("music") || typeStr.contains("sound") -> com.example.model.TrackType.AUDIO
                typeStr.contains("text") || typeStr.contains("caption") || typeStr.contains("sub") -> com.example.model.TrackType.TEXT
                typeStr.contains("overlay") || typeStr.contains("pip") -> com.example.model.TrackType.OVERLAY
                else -> com.example.model.TrackType.MAIN_MEDIA
            }

            val id = itemObj.optString("id", "item_${i + 1}_${UUID.randomUUID().toString().take(4)}")
            val layerIndex = itemObj.optInt("layer_index", itemObj.optInt("layer", if (trackType == com.example.model.TrackType.TEXT) 10 else 0))

            // Timings
            val startMs = when {
                itemObj.has("startTimeMs") -> itemObj.optLong("startTimeMs")
                itemObj.has("startMs") -> itemObj.optLong("startMs")
                itemObj.has("startTime") -> (itemObj.optDouble("startTime") * 1000).toLong()
                itemObj.has("startSeconds") -> (itemObj.optDouble("startSeconds") * 1000).toLong()
                else -> 0L
            }

            val durMs = when {
                itemObj.has("durationMs") -> itemObj.optLong("durationMs")
                itemObj.has("duration") && itemObj.get("duration") is Number && itemObj.optLong("duration") > 100 -> itemObj.optLong("duration")
                itemObj.has("durationSeconds") -> (itemObj.optDouble("durationSeconds") * 1000).toLong()
                itemObj.has("duration") -> (itemObj.optDouble("duration") * 1000).toLong()
                else -> 3000L
            }

            val assetPath = itemObj.optString("assetPath", itemObj.optString("path", itemObj.optString("file", "")))
            val posX = itemObj.optDouble("posX", itemObj.optDouble("x", 0.5)).toFloat()
            val posY = itemObj.optDouble("posY", itemObj.optDouble("y", 0.5)).toFloat()
            val scale = itemObj.optDouble("scale", 1.0).toFloat()

            val effectName = itemObj.optString("effect_name", itemObj.optString("effect", ""))
            val filterName = itemObj.optString("filter_name", itemObj.optString("filter", "NORMAL"))
            val transitionName = itemObj.optString("transition_name", itemObj.optString("transition", "CROSSFADE"))
            val animIn = itemObj.optString("animation_in", itemObj.optString("animationIn", "POP_UP"))
            val animOut = itemObj.optString("animation_out", itemObj.optString("animationOut", "FADE_IN"))

            val textContent = itemObj.optString("text_content", itemObj.optString("text", ""))
            val textStyle = itemObj.optString("text_style", itemObj.optString("font", "Anton"))

            result.add(
                com.example.model.TimelineItemModel(
                    id = id,
                    track_type = trackType,
                    layer_index = layerIndex,
                    startTimeMs = startMs,
                    durationMs = durMs.coerceAtLeast(300L),
                    assetPath = assetPath,
                    posX = posX,
                    posY = posY,
                    scale = scale,
                    effect_name = effectName,
                    filter_name = filterName,
                    transition_name = transitionName,
                    animation_in = animIn,
                    animation_out = animOut,
                    text_content = textContent,
                    text_style = textStyle
                )
            )
        }
        return result
    }

    /**
     * Serializes timeline item models into a pretty-printed JSON array.
     */
    fun serializeTimelineItemsJson(items: List<com.example.model.TimelineItemModel>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("track_type", when (item.track_type) {
                com.example.model.TrackType.MAIN_MEDIA -> "mainMedia"
                com.example.model.TrackType.AUDIO -> "audio"
                com.example.model.TrackType.TEXT -> "text"
                com.example.model.TrackType.OVERLAY -> "overlay"
            })
            obj.put("layer_index", item.layer_index)
            obj.put("startTimeMs", item.startTimeMs)
            obj.put("durationMs", item.durationMs)
            if (item.assetPath.isNotEmpty()) obj.put("assetPath", item.assetPath)
            obj.put("posX", item.posX)
            obj.put("posY", item.posY)
            obj.put("scale", item.scale)
            if (item.effect_name.isNotEmpty()) obj.put("effect_name", item.effect_name)
            if (item.filter_name != "NORMAL") obj.put("filter_name", item.filter_name)
            if (item.transition_name != "CROSSFADE") obj.put("transition_name", item.transition_name)
            obj.put("animation_in", item.animation_in)
            obj.put("animation_out", item.animation_out)
            if (item.text_content.isNotEmpty()) obj.put("text_content", item.text_content)
            if (item.text_style.isNotEmpty()) obj.put("text_style", item.text_style)
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Formats any JSON string into pretty indented 2-space JSON
     */
    fun formatJson(jsonString: String): Result<String> {
        val trimmed = jsonString.trim()
        return try {
            if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                Result.success(arr.toString(2))
            } else {
                val obj = JSONObject(trimmed)
                Result.success(obj.toString(2))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
