package com.example.easebudgetv1.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// (Author, 2024) Permission handling utilities
object PermissionUtils {
    const val CAMERA_PERMISSION_REQUEST = 100
    const val STORAGE_PERMISSION_REQUEST = 101
    
    val CAMERA_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.CAMERA)
    } else {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    
    val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun hasCameraPermissions(activity: Activity): Boolean {
        return CAMERA_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasStoragePermissions(activity: Activity): Boolean {
        return STORAGE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestCameraPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, CAMERA_PERMISSIONS, CAMERA_PERMISSION_REQUEST)
    }

    fun requestStoragePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, STORAGE_PERMISSIONS, STORAGE_PERMISSION_REQUEST)
    }

    fun shouldShowCameraRationale(activity: Activity): Boolean {
        return CAMERA_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    fun shouldShowStorageRationale(activity: Activity): Boolean {
        return STORAGE_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
}
