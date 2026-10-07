package com.example.model

import android.net.Uri

enum class RenderStatus {
    IDLE,
    PREPARING,
    ENCODING,
    SAVING_TO_GALLERY,
    COMPLETED,
    ERROR
}

data class RenderProgress(
    val status: RenderStatus = RenderStatus.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val fps: Float = 0f,
    val message: String = "",
    val outputFilePath: String? = null,
    val mediaStoreUri: Uri? = null,
    val error: String? = null
)
