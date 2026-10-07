package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.AnimationPreset
import com.example.model.AssPresetStyle
import com.example.model.FilterEffectType
import com.example.model.FramingMode
import com.example.model.ImageSyncItem
import com.example.model.ProjectAspectRatio
import com.example.model.ProjectMetadata
import com.example.model.TextEffectPreset
import com.example.model.TextSyncItem
import com.example.model.TransitionType
import com.example.model.VideoProject
import com.example.util.DualJsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProjectManager(private val context: Context) {

    private val projectsRootDir: File by lazy {
        File(context.filesDir, "projects").apply { mkdirs() }
    }

    fun getProjectDir(projectId: String): File {
        return File(projectsRootDir, projectId).apply { mkdirs() }
    }

    fun getPersistentMediaDir(projectId: String): File {
        return File(getProjectDir(projectId), "persistent_media").apply { mkdirs() }
    }

    /**
     * Lists all saved projects ordered by modifiedAt descending
     */
    suspend fun listProjects(): List<ProjectMetadata> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ProjectMetadata>()
        val projectDirs = projectsRootDir.listFiles { file -> file.isDirectory } ?: emptyArray()

        for (dir in projectDirs) {
            val metaFile = File(dir, "project.json")
            if (metaFile.exists()) {
                try {
                    val json = JSONObject(metaFile.readText())
                    val id = json.optString("id", dir.name)
                    val title = json.optString("title", "Untitled Project")
                    val createdAt = json.optLong("createdAt", dir.lastModified())
                    val modifiedAt = json.optLong("modifiedAt", dir.lastModified())
                    val ratioStr = json.optString("aspectRatio", ProjectAspectRatio.SHORTS_9_16.name)
                    val aspectRatio = try {
                        ProjectAspectRatio.valueOf(ratioStr)
                    } catch (_: Exception) {
                        ProjectAspectRatio.SHORTS_9_16
                    }

                    list.add(
                        ProjectMetadata(
                            id = id,
                            title = title,
                            createdAt = createdAt,
                            modifiedAt = modifiedAt,
                            aspectRatio = aspectRatio
                        )
                    )
                } catch (_: Exception) {}
            }
        }

        list.sortedByDescending { it.modifiedAt }
    }

    /**
     * Loads a project completely from disk
     */
    suspend fun loadProject(projectId: String): VideoProject? = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(projectId)
        val metaFile = File(projectDir, "project.json")
        val imageSyncFile = File(projectDir, "image_sync.json")
        val textSyncFile = File(projectDir, "text_sync.json")

        if (!metaFile.exists()) return@withContext null

        try {
            val metaJson = JSONObject(metaFile.readText())
            val metadata = ProjectMetadata(
                id = metaJson.optString("id", projectId),
                title = metaJson.optString("title", "Project"),
                createdAt = metaJson.optLong("createdAt", System.currentTimeMillis()),
                modifiedAt = metaJson.optLong("modifiedAt", System.currentTimeMillis()),
                aspectRatio = try {
                    ProjectAspectRatio.valueOf(metaJson.optString("aspectRatio", ProjectAspectRatio.SHORTS_9_16.name))
                } catch (_: Exception) {
                    ProjectAspectRatio.SHORTS_9_16
                }
            )

            val images = if (imageSyncFile.exists()) {
                DualJsonParser.parseImageSyncJson(imageSyncFile.readText())
            } else emptyList()

            val subtitles = if (textSyncFile.exists()) {
                DualJsonParser.parseTextSyncJson(textSyncFile.readText())
            } else emptyList()

            val audioSyncFile = File(projectDir, "audio_sync.json")
            val audioTracks = if (audioSyncFile.exists()) {
                DualJsonParser.parseAudioSyncJson(audioSyncFile.readText())
            } else emptyList()

            val overlaySyncFile = File(projectDir, "overlay_sync.json")
            val overlays = if (overlaySyncFile.exists()) {
                DualJsonParser.parseOverlaySyncJson(overlaySyncFile.readText())
            } else emptyList()

            VideoProject(metadata, images, overlays, subtitles, audioTracks)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves project to disk: project.json, image_sync.json, and text_sync.json
     */
    suspend fun saveProject(project: VideoProject) = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(project.metadata.id)

        // 1. Save project.json and projectId_project.json
        val metaJson = JSONObject().apply {
            put("id", project.metadata.id)
            put("title", project.metadata.title)
            put("createdAt", project.metadata.createdAt)
            put("modifiedAt", System.currentTimeMillis())
            put("aspectRatio", project.metadata.aspectRatio.name)
        }
        File(projectDir, "project.json").writeText(metaJson.toString(2))

        // Save projectId_project.json with full multi-layer timeline items
        val timelineItems = project.toProjectModel().items
        val timelineScriptJson = DualJsonParser.serializeTimelineItemsJson(timelineItems)
        File(projectDir, "${project.metadata.id}_project.json").writeText(timelineScriptJson)

        // 2. Save image_sync.json
        File(projectDir, "image_sync.json").writeText(DualJsonParser.serializeImageSyncJson(project.images))

        // 3. Save text_sync.json
        File(projectDir, "text_sync.json").writeText(DualJsonParser.serializeTextSyncJson(project.subtitles))

        // 4. Save audio_sync.json
        File(projectDir, "audio_sync.json").writeText(DualJsonParser.serializeAudioSyncJson(project.audioTracks))

        // 5. Save overlay_sync.json
        File(projectDir, "overlay_sync.json").writeText(DualJsonParser.serializeOverlaySyncJson(project.overlays))
    }

    /**
     * Creates a new project with starter media and viral captions
     */
    suspend fun createNewProject(
        title: String = "Viral Short",
        aspectRatio: ProjectAspectRatio = ProjectAspectRatio.SHORTS_9_16
    ): VideoProject = withContext(Dispatchers.IO) {
        val projectId = "proj_" + UUID.randomUUID().toString().substring(0, 8)
        val projectDir = getProjectDir(projectId)
        val persistentMediaDir = getPersistentMediaDir(projectId)

        val metadata = ProjectMetadata(
            id = projectId,
            title = title,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            aspectRatio = aspectRatio
        )

        val project = VideoProject(
            metadata = metadata,
            images = emptyList(),
            overlays = emptyList(),
            subtitles = emptyList(),
            audioTracks = emptyList()
        )
        saveProject(project)
        project
    }

    /**
     * Copies a gallery-picked image URI into the project's permanent storage
     * Returns the relative filename inside persistent_media/
     */
    suspend fun importMediaToProject(projectId: String, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val persistentDir = getPersistentMediaDir(projectId)
            var extension = "jpg"
            var baseName = "media_${System.currentTimeMillis()}"

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val displayName = cursor.getString(nameIndex)
                    if (!displayName.isNullOrBlank()) {
                        val dot = displayName.lastIndexOf('.')
                        if (dot != -1) {
                            extension = displayName.substring(dot + 1)
                            baseName = displayName.substring(0, dot).take(20).replace("[^a-zA-Z0-9_]".toRegex(), "_")
                        }
                    }
                }
            }

            val targetFilename = "${baseName}_${UUID.randomUUID().toString().take(4)}.$extension"
            val targetFile = File(persistentDir, targetFilename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                targetFilename
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Deletes a project completely
     */
    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectId)
        dir.deleteRecursively()
    }

    /**
     * Exports the project as a shareable ZIP archive
     */
    suspend fun exportProjectZip(projectId: String): File? = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(projectId)
        if (!projectDir.exists()) return@withContext null

        val zipFile = File(context.cacheDir, "SyncCut_Project_$projectId.zip")
        if (zipFile.exists()) zipFile.delete()

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                // 1. Pack all project files (images, audio, JSONs)
                projectDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val relativePath = file.relativeTo(projectDir).path
                        zos.putNextEntry(ZipEntry(relativePath))
                        FileInputStream(file).use { fis ->
                            fis.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }

                // 2. Include the real installable APK from running app package
                try {
                    val apkSource = File(context.applicationInfo.sourceDir)
                    if (apkSource.exists() && apkSource.length() > 1024) {
                        zos.putNextEntry(ZipEntry("FacelessVideoEditor_SamsungA53.apk"))
                        FileInputStream(apkSource).use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                } catch (_: Exception) {}

                // 3. Include installation guide
                val guideText = """
=====================================================
SyncCut: Faceless Video Editor & Subtitle Studio
Samsung Galaxy A53 5G Optimized (Exynos Hardware)
=====================================================

এই জিপ ফাইলটিতে রয়েছে সম্পূর্ণ ইনস্টলেবল APK এবং প্রজেক্টের সব ফাইল।

কিভাবে APK ইনস্টল করবেন:
১. এই জিপ ফাইলটি এক্সট্র্যাক্ট করুন।
২. 'FacelessVideoEditor_SamsungA53.apk' ফাইলটিতে ট্যাপ করুন।
৩. ফোন 'Install unknown apps' চাইলে Settings থেকে Allow করুন।
৪. এরপর Install বাটনে চাপ দিন।
                """.trimIndent()
                zos.putNextEntry(ZipEntry("INSTALL_GUIDE.txt"))
                zos.write(guideText.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 4. Include comprehensive feature directory & manual
                zos.putNextEntry(ZipEntry("APP_FEATURES_DIRECTORY.txt"))
                zos.write(com.example.util.AppFeatureDirectory.FULL_MANUAL_TEXT.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
            zipFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Imports and restores a full project from a user-selected ZIP archive
     */
    suspend fun importProjectFromZip(zipUri: Uri): VideoProject? = withContext(Dispatchers.IO) {
        try {
            val newProjectId = "proj_" + UUID.randomUUID().toString().take(8)
            val targetDir = getProjectDir(newProjectId)

            context.contentResolver.openInputStream(zipUri)?.use { input ->
                java.util.zip.ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val destFile = File(targetDir, entry.name)
                            destFile.parentFile?.mkdirs()
                            FileOutputStream(destFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            // Verify project files
            val metaFile = File(targetDir, "project.json")
            if (!metaFile.exists()) {
                // Synthesize metadata if only image_sync.json and text_sync.json were in the ZIP
                val synthesized = JSONObject().apply {
                    put("id", newProjectId)
                    put("title", "Imported Project")
                    put("createdAt", System.currentTimeMillis())
                    put("modifiedAt", System.currentTimeMillis())
                    put("aspectRatio", ProjectAspectRatio.SHORTS_9_16.name)
                }
                metaFile.writeText(synthesized.toString(2))
            }

            loadProject(newProjectId)
        } catch (_: Exception) {
            null
        }
    }
}
