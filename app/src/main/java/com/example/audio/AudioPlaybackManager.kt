package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.model.AudioTrackItem
import java.io.File

class AudioPlaybackManager(private val context: Context) {

    private val activePlayers = mutableMapOf<String, MediaPlayer>()

    fun syncPlayback(
        audioTracks: List<AudioTrackItem>,
        isPlaying: Boolean,
        currentTimeSeconds: Double
    ) {
        if (!isPlaying || audioTracks.isEmpty()) {
            stopAll()
            return
        }

        // For each audio track, check if it should be playing right now
        for (track in audioTracks) {
            if (track.isMuted) {
                activePlayers[track.id]?.let {
                    it.stop()
                    it.release()
                    activePlayers.remove(track.id)
                }
                continue
            }

            val trackStart = track.startSeconds
            val trackEnd = track.startSeconds + track.durationSeconds

            if (currentTimeSeconds >= trackStart && currentTimeSeconds < trackEnd) {
                val offsetInTrack = (currentTimeSeconds - trackStart) + track.audioOffsetSeconds
                val targetMs = (offsetInTrack * 1000).toInt()

                var player = activePlayers[track.id]
                if (player == null) {
                    player = createPlayerForTrack(track)
                    if (player != null) {
                        try {
                            player.seekTo(targetMs)
                            player.setVolume(track.volume.coerceIn(0f, 1f), track.volume.coerceIn(0f, 1f))
                            player.start()
                            activePlayers[track.id] = player
                        } catch (_: Exception) {
                            player.release()
                        }
                    }
                } else {
                    // Check if player is playing and roughly in sync
                    try {
                        if (!player.isPlaying) {
                            player.seekTo(targetMs)
                            player.start()
                        } else {
                            val currentMs = player.currentPosition
                            if (Math.abs(currentMs - targetMs) > 300) {
                                player.seekTo(targetMs)
                            }
                        }
                    } catch (_: Exception) {}
                }
            } else {
                // Outside active range
                activePlayers[track.id]?.let {
                    try {
                        it.stop()
                        it.release()
                    } catch (_: Exception) {}
                    activePlayers.remove(track.id)
                }
            }
        }
    }

    private fun createPlayerForTrack(track: AudioTrackItem): MediaPlayer? {
        return try {
            val mp = MediaPlayer()
            if (track.audioPath.startsWith("sfx_") || track.audioPath.startsWith("bgm_")) {
                val file = SoundEffectsGenerator.getAudioFile(context, track.audioPath)
                mp.setDataSource(file.absolutePath)
            } else if (track.audioPath.startsWith("content://") || track.audioPath.startsWith("file://")) {
                mp.setDataSource(context, Uri.parse(track.audioPath))
            } else {
                val f = File(track.audioPath)
                if (f.exists()) {
                    mp.setDataSource(f.absolutePath)
                } else {
                    return null
                }
            }
            mp.prepare()
            mp
        } catch (_: Exception) {
            null
        }
    }

    fun stopAll() {
        for ((_, player) in activePlayers) {
            try {
                player.stop()
                player.release()
            } catch (_: Exception) {}
        }
        activePlayers.clear()
    }
}
