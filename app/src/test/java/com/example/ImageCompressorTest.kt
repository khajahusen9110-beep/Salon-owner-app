package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.util.ImageCompressor
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.random.Random

/** A large, detailed photo must come out as a JPEG of at most 50 KB within the size limits. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ImageCompressorTest {

    private fun noisyPhoto(width: Int, height: Int): Uri {
        val random = Random(42)
        val pixels = IntArray(width * height) { i ->
            val x = i % width
            val y = i / width
            // Gradient + noise: hard to compress, like a real camera photo.
            val r = (x * 255 / width + random.nextInt(60)).coerceIn(0, 255)
            val g = (y * 255 / height + random.nextInt(60)).coerceIn(0, 255)
            val b = random.nextInt(256)
            (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        val bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        val file = File.createTempFile("photo", ".jpg")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        return Uri.fromFile(file)
    }

    @Test
    fun bannerIsAtMost50KbAndWithinSize() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val out = ImageCompressor.compress(context, noisyPhoto(3000, 2000), ImageCompressor.Kind.BANNER)
        val decoded = BitmapFactory.decodeByteArray(out, 0, out.size)
        println("banner: ${out.size} bytes, ${decoded.width}x${decoded.height}")
        assertTrue("size ${out.size}", out.size <= ImageCompressor.MAX_BYTES)
        assertTrue(decoded.width <= 1280 && decoded.height <= 720)
    }

    @Test
    fun photoIsAtMost50KbAndWithinSize() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val out = ImageCompressor.compress(context, noisyPhoto(2400, 3200), ImageCompressor.Kind.PHOTO)
        val decoded = BitmapFactory.decodeByteArray(out, 0, out.size)
        println("photo: ${out.size} bytes, ${decoded.width}x${decoded.height}")
        assertTrue("size ${out.size}", out.size <= ImageCompressor.MAX_BYTES)
        assertTrue(decoded.width <= 600 && decoded.height <= 600)
    }
}
