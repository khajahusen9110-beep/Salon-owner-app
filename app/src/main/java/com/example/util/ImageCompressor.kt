package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Shrinks a picked photo to a small JPEG before upload: at most [MAX_BYTES] (50 KB), usually 30-50 KB.
 * Banners keep a wide size; stylist/service/category photos are square-ish thumbnails.
 */
object ImageCompressor {
    const val MAX_BYTES = 50 * 1024

    enum class Kind(val maxWidth: Int, val maxHeight: Int) {
        BANNER(1280, 720),
        PHOTO(600, 600)
    }

    class ImageException(message: String) : Exception(message)

    fun compress(context: Context, uri: Uri, kind: Kind): ByteArray {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw ImageException("This file is not a supported photo.")

        // Decode at roughly 2x the target size to save memory, then scale precisely.
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= kind.maxWidth * 2 || bounds.outHeight / (sample * 2) >= kind.maxHeight * 2) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw ImageException("Couldn't read this photo.")

        val rotation = try {
            resolver.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees() } ?: 0
        } catch (_: Exception) {
            0
        }

        var bitmap = fit(rotate(decoded, rotation), kind.maxWidth, kind.maxHeight)
        try {
            while (true) {
                var quality = 85
                while (quality >= 35) {
                    val bytes = encode(bitmap, quality)
                    if (bytes.size <= MAX_BYTES) return bytes
                    quality -= 10
                }
                // Still too big at low quality: make it smaller and try again.
                val w = (bitmap.width * 0.8f).roundToInt()
                val h = (bitmap.height * 0.8f).roundToInt()
                if (w < 160 || h < 160) return encode(bitmap, 35)
                val smaller = Bitmap.createScaledBitmap(bitmap, w, h, true)
                if (smaller !== bitmap) bitmap.recycle()
                bitmap = smaller
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun encode(bitmap: Bitmap, quality: Int): ByteArray =
        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }

    private fun fit(src: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val scale = minOf(maxW.toFloat() / src.width, maxH.toFloat() / src.height, 1f)
        if (scale >= 1f) return src
        val out = Bitmap.createScaledBitmap(src, max(1, (src.width * scale).roundToInt()), max(1, (src.height * scale).roundToInt()), true)
        if (out !== src) src.recycle()
        return out
    }

    private fun rotate(src: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return src
        val out = Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
        if (out !== src) src.recycle()
        return out
    }

    private fun ExifInterface.rotationDegrees(): Int = when (getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}
