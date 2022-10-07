package org.hyperskill.photoeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import java.io.IOException
import kotlin.math.pow


class ImageProcessor(private val context: Context) {
    private val coerceColor = { color: Int, value: Int -> (color + value).coerceIn(0..255) }
    private val contrast = { color: Int, alpha: Double, avg: Double -> (alpha * (color- avg) + avg).toInt().coerceIn(0..255) }
    //private val width = loadedImage.width
    //private val height = loadedImage.height
    fun createBitmap(uri: Uri): Bitmap {
        val loadedImage = BitmapFactory
            .decodeStream(context.contentResolver.openInputStream(uri))
            ?: throw IOException("No such file or Incorrect format")

        return loadedImage
    }

    fun changeBrightness(brightness: Int = 0, contrast: Int = 0, saturation: Int = 0, gamma : Double = 1.0, image: Bitmap): Bitmap {
        if (brightness == 0 &&  contrast == 0 && saturation == 0 && gamma == 1.0) return image

        val width = image.width
        val height = image.height

        val pixelBuffer = IntArray(width * height)
        val newImage = Bitmap.createBitmap(
            width, height,
            Bitmap.Config.ARGB_8888
        )
        image.getPixels(pixelBuffer, 0, width, 0, 0, width, height)

        if (brightness != 0) {
            for (i in pixelBuffer.indices) {
                val red = coerceColor(Color.red(pixelBuffer[i]), brightness)
                val green = coerceColor(Color.green(pixelBuffer[i]), brightness)
                val blue = coerceColor(Color.blue(pixelBuffer[i]), brightness)
                pixelBuffer[i] = Color.rgb(red, green, blue)
            }
        }

        if (contrast != 0) {
            val alpha: Double = (255 + contrast).toDouble() /
                    (255 - contrast).toDouble()

            var counter: Long = 0
            var totalbrightness = 0.0
            for (i in pixelBuffer.indices) {
                counter++
                totalbrightness += (Color.red(pixelBuffer[i]) +
                        Color.blue(pixelBuffer[i]) +
                        Color.green(pixelBuffer[i])) /3.0
            }
            val avgbrightness = totalbrightness / ( counter)

            for (i in pixelBuffer.indices) {
                val red = contrast(Color.red(pixelBuffer[i]), alpha, avgbrightness)
                val green = contrast(Color.green(pixelBuffer[i]), alpha, avgbrightness)
                val blue = contrast(Color.blue(pixelBuffer[i]), alpha, avgbrightness)
                pixelBuffer[i] = Color.rgb(red, green, blue)
            }
        }

        if (saturation != 0) {
            val alpha: Double = (255 + saturation).toDouble() /
                    (255 - saturation).toDouble()
            for (i in pixelBuffer.indices) {
                val rgbavg = ((Color.red(pixelBuffer[i])+Color.green(pixelBuffer[i])+Color.blue(pixelBuffer[i])).toDouble() / 3)

                val red = contrast(Color.red(pixelBuffer[i]), alpha, rgbavg)
                val green = contrast(Color.green(pixelBuffer[i]), alpha, rgbavg)
                val blue = contrast(Color.blue(pixelBuffer[i]), alpha, rgbavg)
                pixelBuffer[i] = Color.rgb(red, green, blue)
            }
        }

        if (gamma != 1.0) {
            for (i in pixelBuffer.indices) {
                val red = ((Color.red(pixelBuffer[i]).toDouble() / 255.0).pow(gamma) * 255).toInt().coerceIn(0..255)
                val green = ((Color.green(pixelBuffer[i]).toDouble() / 255.0).pow(gamma) * 255).toInt().coerceIn(0..255)
                val blue = ((Color.blue(pixelBuffer[i]).toDouble() / 255.0).pow(gamma) * 255).toInt().coerceIn(0..255)
                pixelBuffer[i] = Color.rgb(red, green, blue)
            }
        }
        newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
        return newImage
    }
}