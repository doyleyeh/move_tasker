package com.example.autosorter.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {
    private val legacyPermissions: Array<String>
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> buildList {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
            else -> emptyArray()
        }

    fun legacyPermissionList(): Array<String> =
        if (supportsLegacyPermissions()) legacyPermissions else emptyArray()

    fun hasMediaPermissions(context: Context): Boolean {
        val permissions = legacyPermissions
        if (permissions.isEmpty()) return true
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasAllFilesAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            hasMediaPermissions(context)
        }
    }

    fun supportsLegacyPermissions(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    fun buildManageAllFilesIntent(context: Context): Intent {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        }
        return intent.apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
