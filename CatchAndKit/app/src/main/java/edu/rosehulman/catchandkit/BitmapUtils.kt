package edu.rosehulman.catchandkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

object BitmapUtils {
    fun scaleToFit(context: Context, localPath: String, targetW: Int, targetH: Int): Bitmap? {
        Log.d(Constants.TAG, "Scaling to fit: $localPath ($targetW x $targetH)")
        return if (localPath.startsWith("content")) {
            BitmapUtils.scaleContentBitmapToFit(context, localPath, targetW, targetH)
        } else if (localPath.startsWith("/storage")) {
            BitmapUtils.scaleBitmapToFit(localPath, targetW, targetH)
        } else {
            null
        }
    }

    private fun scaleBitmapToFit(localPath: String, targetW: Int, targetH: Int): Bitmap {
        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(localPath, this)
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scaleToFit down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        return BitmapFactory.decodeFile(localPath, bmOptions)
    }

    private fun scaleContentBitmapToFit(context: Context, localPath: String, targetW: Int, targetH: Int): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(localPath))
        val photoW = bitmap.width
        val photoH = bitmap.height
        val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)
        return Bitmap.createScaledBitmap(bitmap, photoW / scaleFactor, photoH / scaleFactor, true)
    }

    fun getFullSize(context: Context, localPath: String): Bitmap {
        Log.d(Constants.TAG, "Scaling to keep full size : $localPath")
        return if (localPath.startsWith("content")) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(localPath))
        } else {
            BitmapFactory.decodeFile(localPath)
        }
    }

    fun scaleByRatio(context: Context, localPath: String, ratio: Int): Bitmap? {
        Log.d(Constants.TAG, "Scaling by ratio: $localPath")
        return if (localPath.startsWith("content")) {
            BitmapUtils.scaleContentBitmapByRatio(context, localPath, ratio)
        } else if (localPath.startsWith("/storage")) {
            BitmapUtils.scaleBitmapByRatioAlternate(localPath, ratio)
        } else {
            null
        }
    }

    private fun scaleContentBitmapByRatio(context: Context, localPath: String, ratio: Int): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(localPath))
        val photoW = bitmap.width
        val photoH = bitmap.height
        return Bitmap.createScaledBitmap(bitmap, photoW / ratio, photoH / ratio, true)
    }

    private fun scaleBitmapByRatio(localPath: String, ratio: Int): Bitmap {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = ratio
        return BitmapFactory.decodeFile(localPath, bmOptions)
    }

    private fun scaleBitmapByRatioAlternate(localPath: String, ratio: Int): Bitmap {
        val bitmap = BitmapFactory.decodeFile(localPath)
        val photoW = bitmap.width
        val photoH = bitmap.height
        Log.d(Constants.TAG, "Dims are ($photoW, $photoH)")
        return Bitmap.createScaledBitmap(bitmap, photoW / ratio, photoH / ratio, true)
    }

}