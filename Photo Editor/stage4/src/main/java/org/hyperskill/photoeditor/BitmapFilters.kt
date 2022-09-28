package org.hyperskill.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.set


object BitmapFilters {

    fun Bitmap.brightenCopy(brightnessValue: Int): Bitmap {
        val bitmap = this
        val height = bitmap.height
        val width = bitmap.width
        val copy = bitmap.copy(Bitmap.Config.RGB_565, true)

        for(y in 0 until height) {
            for(x in 0 until width) {
                copy[x, y] = bitmap.brightenPixel(x, y, brightnessValue)
            }
        }
        return copy
    }

    fun Bitmap.contrastedCopy(contrastValue: Int, averageBrightness: Int): Bitmap {
        val bitmap = this
        val height = bitmap.height
        val width = bitmap.width
        val copy = bitmap.copy(Bitmap.Config.RGB_565, true)

        for(y in 0 until height) {
            for(x in 0 until width) {
                copy[x, y] = bitmap.contrastPixel(x, y, contrastValue , averageBrightness)
            }
        }
        return copy
    }

    @ColorInt
    private fun Bitmap.brightenPixel(x: Int, y: Int, value: Int): Int {
        val (oldRed, oldGreen, oldBlue) = this.getPixel(x, y).extractRGB()

        val updateValue = { newValue: Int ->
            when {
                newValue > 255 -> 255
                newValue < 0 -> 0
                else -> newValue
            }
        }

        val red = updateValue(oldRed + value)
        val blue = updateValue(oldBlue + value)
        val green = updateValue(oldGreen + value)

        return Color.rgb(red, green, blue)
    }

    @ColorInt
    fun Bitmap.contrastPixel(x: Int, y: Int, contrast: Int, averageBrightness: Int): Int {
        val alpha = (255 + contrast).toDouble() / (255 - contrast)
        val (oldRed, oldGreen, oldBlue) = this.getPixel(x, y).extractRGB()


        val updateValue = { isolatedColor: Int ->
            val newValue = (alpha * (isolatedColor - averageBrightness) + averageBrightness).toInt()
            when {
                newValue > 255 -> 255
                newValue < 0 -> 0
                else -> newValue
            }
        }

        val red = updateValue(oldRed)
        val blue = updateValue(oldBlue)
        val green = updateValue(oldGreen)

        return Color.rgb(red, green, blue)
    }

    fun Bitmap.calculateBrightnessMean(): Int {
        val bitmap = this
        val height = bitmap.height
        val width = bitmap.width
        var total = 0

        for(y in 0 until height) {
            for(x in 0 until width) {
                val (red, green, blue) = this.getPixel(x, y).extractRGB()
                total += red + green + blue
            }
        }

        return total / (width * height * 3)
    }



    private fun Int.extractRGB(): Triple<Int, Int, Int> {
        val color = this
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        return Triple(red, green, blue)
    }
}

