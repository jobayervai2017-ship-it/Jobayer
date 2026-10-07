package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object ApkInstaller {

    /**
     * Launches the native Android / Samsung Package Installer for an APK file
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            Toast.makeText(context, "APK file not found or corrupted", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if Unknown App installation permission is granted on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(context, "Please allow 'Install unknown apps' permission to install update", Toast.LENGTH_LONG).show()
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open package installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Extracts an APK from a user-selected ZIP or APK file URI, then triggers installation
     */
    suspend fun extractAndInstall(context: Context, fileUri: Uri): Result<File> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val tempDir = File(context.cacheDir, "install_temp").apply {
                deleteRecursively()
                mkdirs()
            }

            // Check if user selected .apk directly
            val mimeType = contentResolver.getType(fileUri)
            val isZip = mimeType?.contains("zip") == true || fileUri.toString().endsWith(".zip", ignoreCase = true)

            if (!isZip) {
                // Assume it might be an APK directly
                val targetApk = File(tempDir, "update.apk")
                contentResolver.openInputStream(fileUri)?.use { input ->
                    FileOutputStream(targetApk).use { output ->
                        input.copyTo(output)
                    }
                }
                if (targetApk.exists() && targetApk.length() > 1024) {
                    return@withContext Result.success(targetApk)
                }
            }

            // If it's a ZIP, unpack and search for .apk
            var foundApk: File? = null
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && entry.name.endsWith(".apk", ignoreCase = true)) {
                            val apkFile = File(tempDir, File(entry.name).name)
                            FileOutputStream(apkFile).use { fos ->
                                zis.copyTo(fos)
                            }
                            if (apkFile.exists() && apkFile.length() > 1024) {
                                foundApk = apkFile
                                break
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            if (foundApk != null) {
                Result.success(foundApk!!)
            } else {
                Result.failure(Exception("No valid .apk found inside the selected file."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
