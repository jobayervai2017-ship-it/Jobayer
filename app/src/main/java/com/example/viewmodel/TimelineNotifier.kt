package com.example.viewmodel

import com.example.model.ProjectModel
import com.example.model.TimelineItemModel
import com.example.model.TrackType
import kotlinx.coroutines.flow.StateFlow

/**
 * Centralized TimelineNotifier architecture for high-performance multi-layer editing.
 * Controls project state, tracks currentTimeMs/currentTimeSeconds, zoom, and handles all layer modifications.
 */
interface TimelineNotifier {
    val projectModel: StateFlow<ProjectModel>
    val currentTimeMs: StateFlow<Long>
    val currentTimeSeconds: StateFlow<Double>
    val isPlaying: StateFlow<Boolean>
    val zoomScale: StateFlow<Float>

    fun seekTo(timeMs: Long)
    fun seekToSeconds(seconds: Double)
    fun play()
    fun pause()
    fun togglePlay()
    fun setZoom(scale: Float)

    fun updateTextPosition(itemId: String, posX: Float, posY: Float)
    fun splitItem(itemId: String, atMs: Long)
    fun trimItem(itemId: String, startTimeMs: Long, durationMs: Long)
    fun applyEffect(itemId: String, effectName: String)
    fun applyFilter(itemId: String, filterName: String)
    fun applyTransition(itemId: String, transitionName: String)
    fun applyAnimation(itemId: String, animationIn: String, animationOut: String)
    fun importJsonScript(rawJson: String): Result<Int>
}
