package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectManager
import com.example.model.AssPresetStyle
import com.example.model.AudioTrackItem
import com.example.model.ExportConfig
import com.example.model.ExportResolution
import com.example.model.FilterEffectType
import com.example.model.FramingMode
import com.example.model.ImageSyncItem
import com.example.model.OverlayImageItem
import com.example.model.ProjectAspectRatio
import com.example.model.ProjectMetadata
import com.example.model.RenderProgress
import com.example.model.RenderStatus
import com.example.model.TextSyncItem
import com.example.model.TransitionType
import com.example.model.VideoProject
import com.example.util.DualJsonParser
import com.example.video.HardwareVideoEncoder
import com.example.model.ProjectModel
import com.example.model.TimelineItemModel
import com.example.model.TrackType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class EditorViewModel(application: Application) : AndroidViewModel(application), TimelineNotifier {

    private val projectManager = ProjectManager(application)
    private val videoEncoder = HardwareVideoEncoder(application)

    // Project List
    private val _projects = MutableStateFlow<List<ProjectMetadata>>(emptyList())
    val projects: StateFlow<List<ProjectMetadata>> = _projects.asStateFlow()

    // Active Project
    private val _activeProject = MutableStateFlow<VideoProject?>(null)
    val activeProject: StateFlow<VideoProject?> = _activeProject.asStateFlow()

    private val _projectModel = MutableStateFlow<ProjectModel>(ProjectModel(id = "", name = "Empty Project"))
    override val projectModel: StateFlow<ProjectModel> = _projectModel.asStateFlow()

    // Playback State
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeSeconds = MutableStateFlow(0.0)
    override val currentTimeSeconds: StateFlow<Double> = _currentTimeSeconds.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    override val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _zoomScale = MutableStateFlow(1.0f)
    override val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    // Selection
    private val _selectedSceneIndex = MutableStateFlow<Int?>(0)
    val selectedSceneIndex: StateFlow<Int?> = _selectedSceneIndex.asStateFlow()

    private val _selectedSubtitleId = MutableStateFlow<String?>(null)
    val selectedSubtitleId: StateFlow<String?> = _selectedSubtitleId.asStateFlow()

    private val _selectedAudioId = MutableStateFlow<String?>(null)
    val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

    private val _selectedOverlayId = MutableStateFlow<String?>(null)
    val selectedOverlayId: StateFlow<String?> = _selectedOverlayId.asStateFlow()

    private val audioPlaybackManager = com.example.audio.AudioPlaybackManager(getApplication())

    // Export Progress
    private val _renderProgress = MutableStateFlow(RenderProgress())
    val renderProgress: StateFlow<RenderProgress> = _renderProgress.asStateFlow()

    private var playbackJob: Job? = null
    private var autoSaveJob: Job? = null
    private var exportJob: Job? = null

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            val list = projectManager.listProjects()
            _projects.value = list
            if (_activeProject.value == null && list.isNotEmpty()) {
                selectProject(list.first().id)
            } else if (list.isEmpty()) {
                // Create first starter project
                val starter = projectManager.createNewProject("My Viral Short")
                _activeProject.value = starter
                _projects.value = listOf(starter.metadata)
            }
        }
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            pause()
            val project = projectManager.loadProject(projectId)
            _activeProject.value = project
            _projectModel.value = project?.toProjectModel() ?: ProjectModel(id = projectId, name = "Project")
            _currentTimeSeconds.value = 0.0
            _currentTimeMs.value = 0L
            _selectedSceneIndex.value = if (project?.images?.isNotEmpty() == true) 0 else null
            _selectedSubtitleId.value = project?.subtitles?.firstOrNull()?.id
        }
    }

    fun createNewProject(title: String, aspectRatio: ProjectAspectRatio) {
        viewModelScope.launch {
            pause()
            val newProject = projectManager.createNewProject(title, aspectRatio)
            _activeProject.value = newProject
            _projectModel.value = newProject.toProjectModel()
            _currentTimeSeconds.value = 0.0
            _currentTimeMs.value = 0L
            _selectedSceneIndex.value = 0
            _selectedSubtitleId.value = newProject.subtitles.firstOrNull()?.id
            _projects.value = projectManager.listProjects()
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectManager.deleteProject(projectId)
            val updated = projectManager.listProjects()
            _projects.value = updated
            if (_activeProject.value?.metadata?.id == projectId) {
                if (updated.isNotEmpty()) {
                    selectProject(updated.first().id)
                } else {
                    _activeProject.value = null
                    _projectModel.value = ProjectModel(id = "", name = "Empty Project")
                }
            }
        }
    }

    // Playback Controls
    override fun togglePlay() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    override fun play() {
        val proj = _activeProject.value ?: return
        val maxDuration = proj.totalDurationSeconds
        if (_currentTimeSeconds.value >= maxDuration) {
            _currentTimeSeconds.value = 0.0
            _currentTimeMs.value = 0L
        }

        _isPlaying.value = true
        audioPlaybackManager.syncPlayback(proj.audioTracks, true, _currentTimeSeconds.value)

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch(Dispatchers.Default) {
            val intervalMs = 33L // ~30 fps
            while (isActive && _isPlaying.value) {
                delay(intervalMs)
                val newTime = _currentTimeSeconds.value + (intervalMs / 1000.0)
                if (newTime >= maxDuration) {
                    _currentTimeSeconds.value = 0.0 // Loop back smoothly
                    _currentTimeMs.value = 0L
                } else {
                    _currentTimeSeconds.value = newTime
                    _currentTimeMs.value = (newTime * 1000).toLong()
                }
                audioPlaybackManager.syncPlayback(proj.audioTracks, _isPlaying.value, _currentTimeSeconds.value)
            }
        }
    }

    override fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        audioPlaybackManager.stopAll()
    }

    fun seekTo(seconds: Double) {
        val proj = _activeProject.value
        val maxDuration = proj?.totalDurationSeconds ?: 10.0
        val clamped = seconds.coerceIn(0.0, maxDuration)
        _currentTimeSeconds.value = clamped
        _currentTimeMs.value = (clamped * 1000).toLong()
        if (proj != null) {
            audioPlaybackManager.syncPlayback(proj.audioTracks, _isPlaying.value, clamped)
        }
    }

    override fun seekTo(timeMs: Long) {
        seekTo(timeMs / 1000.0)
    }

    override fun seekToSeconds(seconds: Double) {
        seekTo(seconds)
    }

    override fun setZoom(scale: Float) {
        _zoomScale.value = scale.coerceIn(0.5f, 3.5f)
    }

    // Selection
    fun selectScene(index: Int) {
        _selectedSceneIndex.value = index
        // Seek to the start of this scene
        val images = _activeProject.value?.images ?: return
        var start = 0.0
        for (i in 0 until index.coerceAtMost(images.size)) {
            start += images[i].durationSeconds
        }
        seekTo(start)
    }

    fun selectSubtitle(id: String?) {
        _selectedSubtitleId.value = id
        if (id != null) {
            val sub = _activeProject.value?.subtitles?.find { it.id == id }
            if (sub != null) {
                seekTo(sub.startSeconds)
            }
        }
    }

    // Project Mutators (with debounced auto-save)
    private fun mutateActiveProject(transform: (VideoProject) -> VideoProject) {
        val current = _activeProject.value ?: return
        val updated = transform(current)
        _activeProject.value = updated
        _projectModel.value = updated.toProjectModel()
        triggerAutoSave(updated)
    }

    private fun triggerAutoSave(project: VideoProject) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000L) // 2-second debounced auto-save engine
            projectManager.saveProject(project)
        }
    }

    // TimelineNotifier Modification Methods
    override fun updateTextPosition(itemId: String, posX: Float, posY: Float) {
        updateSubtitlePosition(itemId, posX, posY)
    }

    override fun splitItem(itemId: String, atMs: Long) {
        val proj = _activeProject.value ?: return
        val sceneIdx = proj.images.indexOfFirst { it.id == itemId }
        if (sceneIdx != -1) {
            splitScene(sceneIdx)
        }
    }

    override fun trimItem(itemId: String, startTimeMs: Long, durationMs: Long) {
        val proj = _activeProject.value ?: return
        val durSec = (durationMs / 1000.0).coerceAtLeast(0.3)
        val startSec = (startTimeMs / 1000.0).coerceAtLeast(0.0)

        val scene = proj.images.find { it.id == itemId }
        if (scene != null) {
            updateSceneDuration(itemId, durSec)
            return
        }

        val sub = proj.subtitles.find { it.id == itemId }
        if (sub != null) {
            mutateActiveProject { p ->
                p.copy(subtitles = p.subtitles.map {
                    if (it.id == itemId) it.copy(startSeconds = startSec, endSeconds = startSec + durSec) else it
                })
            }
            return
        }

        val audio = proj.audioTracks.find { it.id == itemId }
        if (audio != null) {
            updateAudioTiming(itemId, startSec, durSec)
            return
        }

        val overlay = proj.overlays.find { it.id == itemId }
        if (overlay != null) {
            updateOverlayTiming(itemId, startSec, durSec)
        }
    }

    fun updateSubtitleEffect(id: String, effect: com.example.model.TextEffectPreset) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) it.copy(textEffect = effect) else it
            }
            proj.copy(subtitles = updated)
        }
    }

    fun updateSubtitleAnimation(id: String, animation: com.example.model.AnimationPreset) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) it.copy(animationPreset = animation) else it
            }
            proj.copy(subtitles = updated)
        }
    }

    override fun applyEffect(itemId: String, effectName: String) {
        val proj = _activeProject.value ?: return
        val textItem = proj.subtitles.find { it.id == itemId }
        if (textItem != null) {
            val fx = try {
                com.example.model.TextEffectPreset.valueOf(effectName.uppercase())
            } catch (_: Exception) {
                com.example.model.TextEffectPreset.entries.find { it.name.equals(effectName, ignoreCase = true) } ?: com.example.model.TextEffectPreset.STROKE_3D
            }
            updateSubtitleEffect(itemId, fx)
        }
    }

    override fun applyFilter(itemId: String, filterName: String) {
        val proj = _activeProject.value ?: return
        val scene = proj.images.find { it.id == itemId }
        if (scene != null) {
            val filt = try {
                FilterEffectType.valueOf(filterName.uppercase())
            } catch (_: Exception) {
                FilterEffectType.entries.find { it.name.equals(filterName, ignoreCase = true) } ?: FilterEffectType.NORMAL
            }
            updateSceneFilter(itemId, filt)
        }
    }

    override fun applyTransition(itemId: String, transitionName: String) {
        val proj = _activeProject.value ?: return
        val scene = proj.images.find { it.id == itemId }
        if (scene != null) {
            val trans = try {
                TransitionType.valueOf(transitionName.uppercase())
            } catch (_: Exception) {
                TransitionType.entries.find { it.name.equals(transitionName, ignoreCase = true) } ?: TransitionType.CROSSFADE
            }
            updateSceneTransition(itemId, trans)
        }
    }

    override fun applyAnimation(itemId: String, animationIn: String, animationOut: String) {
        val proj = _activeProject.value ?: return
        val sub = proj.subtitles.find { it.id == itemId }
        if (sub != null) {
            val anim = try {
                com.example.model.AnimationPreset.valueOf(animationIn.uppercase())
            } catch (_: Exception) {
                com.example.model.AnimationPreset.entries.find { it.name.equals(animationIn, ignoreCase = true) } ?: com.example.model.AnimationPreset.POP_UP
            }
            updateSubtitleAnimation(itemId, anim)
        }
    }

    override fun importJsonScript(rawJson: String): Result<Int> {
        return try {
            val items = DualJsonParser.parseJsonAutomationScript(rawJson)
            if (items.isEmpty()) {
                return Result.failure(IllegalArgumentException("No valid timeline items found in script"))
            }
            val currentMeta = _activeProject.value?.metadata
            val projModel = ProjectModel(
                id = currentMeta?.id ?: ("proj_" + UUID.randomUUID().toString().take(8)),
                name = currentMeta?.title ?: "Automated Script Project",
                canvasSize = currentMeta?.aspectRatio?.label ?: "9:16",
                items = items
            )
            val newVideoProject = projModel.toVideoProject()
            _activeProject.value = newVideoProject
            _projectModel.value = projModel
            _currentTimeSeconds.value = 0.0
            _currentTimeMs.value = 0L
            _selectedSceneIndex.value = if (newVideoProject.images.isNotEmpty()) 0 else null
            _selectedSubtitleId.value = newVideoProject.subtitles.firstOrNull()?.id
            triggerAutoSave(newVideoProject)
            Result.success(items.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateAspectRatio(ratio: ProjectAspectRatio) {
        mutateActiveProject { it.copy(metadata = it.metadata.copy(aspectRatio = ratio)) }
    }

    fun updateSceneDuration(sceneId: String, durationSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.images.map {
                if (it.id == sceneId) it.copy(durationSeconds = maxOf(0.5, durationSeconds)) else it
            }
            proj.copy(images = updated)
        }
    }

    fun updateSceneTransition(sceneId: String, transition: TransitionType) {
        mutateActiveProject { proj ->
            val updated = proj.images.map {
                if (it.id == sceneId) it.copy(transition = transition) else it
            }
            proj.copy(images = updated)
        }
    }

    fun updateSceneFilter(sceneId: String, filter: FilterEffectType) {
        mutateActiveProject { proj ->
            val updated = proj.images.map {
                if (it.id == sceneId) it.copy(filterEffect = filter) else it
            }
            proj.copy(images = updated)
        }
    }

    fun removeScene(sceneId: String) {
        mutateActiveProject { proj ->
            val updated = proj.images.filter { it.id != sceneId }
            proj.copy(images = updated)
        }
    }

    fun addMediaUris(uris: List<Uri>) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val newItems = mutableListOf<ImageSyncItem>()
            for (uri in uris) {
                val savedFilename = projectManager.importMediaToProject(proj.metadata.id, uri)
                if (savedFilename != null) {
                    newItems.add(
                        ImageSyncItem(
                            id = "scene_${System.currentTimeMillis()}_${newItems.size}",
                            imagePath = savedFilename,
                            durationSeconds = 3.0,
                            transition = TransitionType.CROSSFADE,
                            filterEffect = FilterEffectType.NORMAL,
                            framingMode = FramingMode.CROP_TO_FILL
                        )
                    )
                }
            }

            if (newItems.isNotEmpty()) {
                mutateActiveProject { it.copy(images = it.images + newItems) }
            }
        }
    }

    // Subtitle Actions
    fun addSubtitle(text: String = "NEW CAPTION", startSeconds: Double? = null, endSeconds: Double? = null) {
        val proj = _activeProject.value ?: return
        val start = startSeconds ?: _currentTimeSeconds.value
        val end = endSeconds ?: (start + 2.5)

        val newSub = TextSyncItem(
            id = "sub_${System.currentTimeMillis()}",
            text = text,
            startSeconds = start,
            endSeconds = end,
            fontName = "Anton",
            fontSize = 42,
            textColorHex = "#FFE600",
            outlineColorHex = "#000000",
            posX = 0.5f,
            posY = 0.78f
        )

        mutateActiveProject { it.copy(subtitles = it.subtitles + newSub) }
        _selectedSubtitleId.value = newSub.id
    }

    fun updateSubtitleText(id: String, newText: String) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) it.copy(text = newText) else it
            }
            proj.copy(subtitles = updated)
        }
    }

    fun updateSubtitleTimes(id: String, startSeconds: Double, endSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) it.copy(
                    startSeconds = maxOf(0.0, startSeconds),
                    endSeconds = maxOf(startSeconds + 0.2, endSeconds)
                ) else it
            }
            proj.copy(subtitles = updated)
        }
    }

    fun updateSubtitlePosition(id: String, posX: Float, posY: Float) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) it.copy(
                    posX = posX.coerceIn(0.05f, 0.95f),
                    posY = posY.coerceIn(0.05f, 0.95f)
                ) else it
            }
            proj.copy(subtitles = updated)
        }
    }

    fun applySubtitlePreset(id: String, preset: AssPresetStyle) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) {
                    it.copy(
                        fontName = preset.fontName,
                        fontSize = preset.fontSize,
                        textColorHex = preset.textColorHex,
                        outlineColorHex = preset.outlineColorHex,
                        animationPreset = preset.animationPreset,
                        textEffect = preset.textEffect
                    )
                } else it
            }
            proj.copy(subtitles = updated)
        }
    }

    fun applyPresetToAllSubtitles(preset: AssPresetStyle) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                it.copy(
                    fontName = preset.fontName,
                    fontSize = preset.fontSize,
                    textColorHex = preset.textColorHex,
                    outlineColorHex = preset.outlineColorHex,
                    animationPreset = preset.animationPreset,
                    textEffect = preset.textEffect
                )
            }
            proj.copy(subtitles = updated)
        }
    }

    fun removeSubtitle(id: String) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.filter { it.id != id }
            proj.copy(subtitles = updated)
        }
        if (_selectedSubtitleId.value == id) {
            _selectedSubtitleId.value = null
        }
    }

    fun duplicateSubtitle(id: String) {
        val proj = _activeProject.value ?: return
        val sub = proj.subtitles.find { it.id == id } ?: return
        val newSub = sub.copy(
            id = "sub_" + UUID.randomUUID().toString().take(6),
            startSeconds = sub.endSeconds + 0.1,
            endSeconds = sub.endSeconds + 0.1 + (sub.endSeconds - sub.startSeconds)
        )
        mutateActiveProject { it.copy(subtitles = it.subtitles + newSub) }
        _selectedSubtitleId.value = newSub.id
    }

    fun updateSubtitleTiming(id: String, startSeconds: Double, endSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.subtitles.map {
                if (it.id == id) it.copy(
                    startSeconds = startSeconds.coerceAtLeast(0.0),
                    endSeconds = maxOf(startSeconds + 0.2, endSeconds)
                ) else it
            }
            proj.copy(subtitles = updated)
        }
    }

    // Audio Track Studio Management
    fun selectAudioTrack(id: String?) {
        _selectedAudioId.value = id
    }

    fun addAudioTrack(pathOrPreset: String, title: String, isBgm: Boolean) {
        val newAudio = AudioTrackItem(
            id = "audio_" + UUID.randomUUID().toString().take(6),
            audioPath = pathOrPreset,
            title = title,
            startSeconds = _currentTimeSeconds.value,
            durationSeconds = if (isBgm) 12.0 else 1.5,
            volume = if (isBgm) 0.8f else 1.0f,
            isBgm = isBgm
        )
        mutateActiveProject { proj ->
            proj.copy(audioTracks = proj.audioTracks + newAudio)
        }
        _selectedAudioId.value = newAudio.id
    }

    fun addAudioFromUri(uri: Uri, isBgm: Boolean = true) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val filename = projectManager.importMediaToProject(proj.metadata.id, uri) ?: return@launch
            val fullPath = File(projectManager.getPersistentMediaDir(proj.metadata.id), filename).absolutePath
            val title = filename.substringBeforeLast('.').replace("_", " ")
            val newAudio = AudioTrackItem(
                id = "audio_" + UUID.randomUUID().toString().take(6),
                audioPath = fullPath,
                title = title,
                startSeconds = _currentTimeSeconds.value,
                durationSeconds = if (isBgm) 15.0 else 2.0,
                volume = 0.9f,
                isBgm = isBgm
            )
            mutateActiveProject { it.copy(audioTracks = it.audioTracks + newAudio) }
            _selectedAudioId.value = newAudio.id
        }
    }

    fun nudgeAudioStart(id: String, deltaSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.audioTracks.map {
                if (it.id == id) {
                    val newStart = (it.startSeconds + deltaSeconds).coerceAtLeast(0.0)
                    it.copy(startSeconds = newStart)
                } else it
            }
            proj.copy(audioTracks = updated)
        }
    }

    fun duplicateAudioTrack(id: String) {
        val proj = _activeProject.value ?: return
        val track = proj.audioTracks.find { it.id == id } ?: return
        val newTrack = track.copy(
            id = "audio_" + UUID.randomUUID().toString().take(6),
            startSeconds = track.startSeconds + track.durationSeconds + 0.2
        )
        mutateActiveProject { it.copy(audioTracks = it.audioTracks + newTrack) }
        _selectedAudioId.value = newTrack.id
    }

    fun updateAudioTiming(id: String, startSeconds: Double, durationSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.audioTracks.map {
                if (it.id == id) it.copy(
                    startSeconds = startSeconds.coerceAtLeast(0.0),
                    durationSeconds = durationSeconds.coerceAtLeast(0.2)
                ) else it
            }
            proj.copy(audioTracks = updated)
        }
    }

    fun updateAudioOffset(id: String, offsetSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.audioTracks.map {
                if (it.id == id) it.copy(audioOffsetSeconds = offsetSeconds.coerceAtLeast(0.0)) else it
            }
            proj.copy(audioTracks = updated)
        }
    }

    fun updateAudioVolume(id: String, volume: Float) {
        mutateActiveProject { proj ->
            val updated = proj.audioTracks.map {
                if (it.id == id) it.copy(volume = volume.coerceIn(0f, 2f)) else it
            }
            proj.copy(audioTracks = updated)
        }
    }

    fun toggleAudioMute(id: String) {
        mutateActiveProject { proj ->
            val updated = proj.audioTracks.map {
                if (it.id == id) it.copy(isMuted = !it.isMuted) else it
            }
            proj.copy(audioTracks = updated)
        }
    }

    fun removeAudioTrack(id: String) {
        mutateActiveProject { proj ->
            proj.copy(audioTracks = proj.audioTracks.filter { it.id != id })
        }
        if (_selectedAudioId.value == id) {
            _selectedAudioId.value = null
        }
    }

    // Multi-Layer Overlay / PIP Management
    fun selectOverlay(id: String?) {
        _selectedOverlayId.value = id
    }

    fun addOverlay(uri: Uri) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val filename = projectManager.importMediaToProject(proj.metadata.id, uri) ?: return@launch
            val newOverlay = OverlayImageItem(
                id = "ov_" + UUID.randomUUID().toString().take(6),
                imagePath = filename,
                startSeconds = _currentTimeSeconds.value,
                durationSeconds = 3.0,
                posX = 0.5f,
                posY = 0.5f,
                scale = 0.45f
            )
            mutateActiveProject { it.copy(overlays = it.overlays + newOverlay) }
            _selectedOverlayId.value = newOverlay.id
        }
    }

    fun updateOverlayPosition(id: String, posX: Float, posY: Float, scale: Float = 0.45f) {
        mutateActiveProject { proj ->
            val updated = proj.overlays.map {
                if (it.id == id) it.copy(
                    posX = posX.coerceIn(0f, 1f),
                    posY = posY.coerceIn(0f, 1f),
                    scale = scale.coerceIn(0.1f, 1.5f)
                ) else it
            }
            proj.copy(overlays = updated)
        }
    }

    fun updateOverlayProperties(
        id: String,
        posX: Float,
        posY: Float,
        scale: Float,
        rotation: Float,
        opacity: Float
    ) {
        mutateActiveProject { proj ->
            val updated = proj.overlays.map {
                if (it.id == id) it.copy(
                    posX = posX.coerceIn(0f, 1f),
                    posY = posY.coerceIn(0f, 1f),
                    scale = scale.coerceIn(0.1f, 2.0f),
                    rotation = rotation,
                    opacity = opacity.coerceIn(0.05f, 1.0f)
                ) else it
            }
            proj.copy(overlays = updated)
        }
    }

    fun nudgeOverlayStart(id: String, deltaSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.overlays.map {
                if (it.id == id) {
                    val newStart = (it.startSeconds + deltaSeconds).coerceAtLeast(0.0)
                    it.copy(startSeconds = newStart)
                } else it
            }
            proj.copy(overlays = updated)
        }
    }

    fun duplicateOverlay(id: String) {
        val proj = _activeProject.value ?: return
        val ov = proj.overlays.find { it.id == id } ?: return
        val newOv = ov.copy(
            id = "ov_" + UUID.randomUUID().toString().take(6),
            startSeconds = ov.startSeconds + ov.durationSeconds + 0.1
        )
        mutateActiveProject { it.copy(overlays = it.overlays + newOv) }
        _selectedOverlayId.value = newOv.id
    }

    fun updateOverlayTiming(id: String, startSeconds: Double, durationSeconds: Double) {
        mutateActiveProject { proj ->
            val updated = proj.overlays.map {
                if (it.id == id) it.copy(
                    startSeconds = startSeconds.coerceAtLeast(0.0),
                    durationSeconds = durationSeconds.coerceAtLeast(0.3)
                ) else it
            }
            proj.copy(overlays = updated)
        }
    }

    fun removeOverlay(id: String) {
        mutateActiveProject { proj ->
            proj.copy(overlays = proj.overlays.filter { it.id != id })
        }
        if (_selectedOverlayId.value == id) {
            _selectedOverlayId.value = null
        }
    }

    fun updateSceneMotion(sceneId: String, motion: com.example.model.ImageMotionAnimation) {
        mutateActiveProject { proj ->
            val updated = proj.images.map {
                if (it.id == sceneId) it.copy(motionAnimation = motion) else it
            }
            proj.copy(images = updated)
        }
    }

    // Scene Split & Duplicate
    fun splitScene(index: Int) {
        val proj = _activeProject.value ?: return
        if (index !in proj.images.indices) return
        val scene = proj.images[index]
        if (scene.durationSeconds <= 1.0) return

        val dur1 = scene.durationSeconds / 2.0
        val dur2 = scene.durationSeconds - dur1
        val scene1 = scene.copy(durationSeconds = dur1)
        val scene2 = scene.copy(id = "scene_" + UUID.randomUUID().toString().take(6), durationSeconds = dur2)

        val newImages = proj.images.toMutableList()
        newImages[index] = scene1
        newImages.add(index + 1, scene2)

        mutateActiveProject { it.copy(images = newImages) }
    }

    fun duplicateScene(index: Int) {
        val proj = _activeProject.value ?: return
        if (index !in proj.images.indices) return
        val scene = proj.images[index]
        val dup = scene.copy(id = "scene_" + UUID.randomUUID().toString().take(6))
        val newImages = proj.images.toMutableList()
        newImages.add(index + 1, dup)
        mutateActiveProject { it.copy(images = newImages) }
    }

    // Dual JSON Synchronizer
    fun syncFromJson(imageJson: String, textJson: String): Result<String> {
        return try {
            val parsedImages = DualJsonParser.parseImageSyncJson(imageJson)
            val parsedSubtitles = DualJsonParser.parseTextSyncJson(textJson)

            mutateActiveProject { proj ->
                proj.copy(
                    images = parsedImages.ifEmpty { proj.images },
                    subtitles = parsedSubtitles.ifEmpty { proj.subtitles }
                )
            }
            Result.success("Synchronized ${parsedImages.size} scenes and ${parsedSubtitles.size} captions successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Video Export
    fun startExport(config: ExportConfig = ExportConfig()) {
        val proj = _activeProject.value ?: return
        pause()

        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            _renderProgress.value = RenderProgress(status = RenderStatus.PREPARING, message = "Initializing hardware encoder...")
            val result = videoEncoder.encodeVideo(
                project = proj,
                config = config,
                onProgress = { progress ->
                    _renderProgress.value = progress
                }
            )

            result.onFailure { error ->
                _renderProgress.value = RenderProgress(
                    status = RenderStatus.ERROR,
                    error = error.message ?: "Export failed"
                )
            }
        }
    }

    fun resetExportState() {
        exportJob?.cancel()
        _renderProgress.value = RenderProgress(status = RenderStatus.IDLE)
    }

    fun openExportedVideo() {
        val file = _renderProgress.value.outputFilePath?.let { File(it) } ?: return
        videoEncoder.openVideoInGallery(getApplication(), file, _renderProgress.value.mediaStoreUri)
    }

    fun shareExportedVideo() {
        val file = _renderProgress.value.outputFilePath?.let { File(it) } ?: return
        videoEncoder.shareVideo(getApplication(), file)
    }

    fun exportProjectZip(onComplete: (File?) -> Unit) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val zip = projectManager.exportProjectZip(proj.metadata.id)
            onComplete(zip)
        }
    }

    fun importProjectFromZip(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val imported = projectManager.importProjectFromZip(uri)
            if (imported != null) {
                _projects.value = projectManager.listProjects()
                selectProject(imported.metadata.id)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pause()
    }
}
