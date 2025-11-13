package com.example.henrik_sachdeva_myruns3

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility object for permission handling and bitmap operations.
 */
object Util {

    /**
     * Checks and requests camera and storage permissions if not already granted.
     */
    fun checkPermissions(activity: Activity?) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val hasWritePermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasWritePermission || !hasCameraPermission) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                0
            )
        }
    }

    /**
     * Loads a bitmap from the given image URI.
     * Returns a correctly oriented bitmap if rotation is required.
     */
    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imgUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val matrix = Matrix()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
