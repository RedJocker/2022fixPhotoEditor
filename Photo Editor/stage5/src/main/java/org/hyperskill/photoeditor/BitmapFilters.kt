package org.hyperskill.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.set
import kotlin.math.pow


object BitmapFilters {

    val testedX = 70
    val testedY = 60

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

    fun Bitmap.saturatedCopy(saturationValue: Int, originalBitmap: Bitmap): Bitmap {
        val bitmap = this
        val height = bitmap.height
        val width = bitmap.width
        val copy = bitmap.copy(Bitmap.Config.RGB_565, true)


        for(y in 0 until height) {
            for(x in 0 until width) {
                val (red, green, blue) = originalBitmap.getPixel(x, y).extractRGB()
                val pixelRgbAverage = (red + blue + green) / 3
                copy[x, y] = bitmap.saturatePixel(x, y, saturationValue, pixelRgbAverage)
            }
        }
        return copy
    }

    fun Bitmap.gammaCopy(gammaValue: Float): Bitmap {
        val bitmap = this
        val height = bitmap.height
        val width = bitmap.width
        val copy = bitmap.copy(Bitmap.Config.RGB_565, true)

        for(y in 0 until height) {
            for(x in 0 until width) {
                copy[x, y] = bitmap.gammaPixel(x, y, gammaValue.toDouble())
            }
        }
        return copy
    }

    @ColorInt
    private fun Bitmap.brightenPixel(x: Int, y: Int, value: Int): Int {
        val (oldRed, oldGreen, oldBlue) = this.getPixel(x, y).extractRGB()

        if(x == testedX && y == testedY) {
            println("initial R $oldRed, G $oldGreen, B $oldBlue")
        }

        val updateValue = { newValue: Int ->
            when {
                newValue > 255 -> 255
                newValue < 0 -> 0
                else -> newValue
            }
        }

//        val red = updateValue(oldRed + value - 50)   // should produce "Wrong values after filters been applied. For x=_, y=_ expected: <(__, __, __)> actual: <(__, __, __)>"
        val red = updateValue(oldRed + value)
        val blue = updateValue(oldBlue + value)
        val green = updateValue(oldGreen + value)

        if(x == testedX && y == testedY) {
            println("brightness R $red, G $green, B $blue")
        }

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

//        val red = updateValue(oldRed - 50)   // should produce "Wrong values after filters been applied. For x=_, y=_ expected: <(__, __, __)> actual: <(__, __, __)>"
        val red = updateValue(oldRed)
        val blue = updateValue(oldBlue)
        val green = updateValue(oldGreen)

        if(x == testedX && y == testedY) {
            println("contrast R $red, G $green, B $blue, avgBright $averageBrightness, cAlpha $alpha" )
        }

        return Color.rgb(red, green, blue)
    }

    @ColorInt
    private fun Bitmap.saturatePixel(x: Int, y: Int, saturationValue: Int, pixelRgbAverage: Int): Int {
        val alpha = (255 + saturationValue).toDouble() / (255 - saturationValue)
        val (oldRed, oldGreen, oldBlue) = this.getPixel(x, y).extractRGB()

        val updateValue = { isolatedColor: Int ->
            val newValue = (alpha * (isolatedColor - pixelRgbAverage) + pixelRgbAverage).toInt()
            when {
                newValue > 255 -> 255
                newValue < 0 -> 0
                else -> newValue
            }
        }

//        val red = updateValue(oldRed - 50)   // should produce "Wrong values after filters been applied. For x=_, y=_ expected: <(__, __, __)> actual: <(__, __, __)>"
        val red = updateValue(oldRed)
        val blue = updateValue(oldBlue)
        val green = updateValue(oldGreen)

        if(x == testedX && y == testedY) {
            println("saturation R $red, G $green, B $blue, rgbAvg $pixelRgbAverage, sAlpha $alpha")
        }

        return Color.rgb(red, green, blue)
    }

    @ColorInt
    private fun Bitmap.gammaPixel(x: Int, y: Int, gammaValue: Double): Int {
        val (oldRed, oldGreen, oldBlue) = this.getPixel(x, y).extractRGB()

        val updateValue = { isolatedColor: Int ->
            val newValue = (255 * (isolatedColor / 255.0).pow(gammaValue)).toInt()
            when {
                newValue > 255 -> 255
                newValue < 0 -> 0
                else -> newValue
            }
        }

//        val red = updateValue(oldRed - 50)   // should produce "Wrong values after filters been applied. For x=_, y=_ expected: <(__, __, __)> actual: <(__, __, __)>"
        val red = updateValue(oldRed)
        val blue = updateValue(oldBlue)
        val green = updateValue(oldGreen)

        if(x == testedX && y == testedY) {
            println("gamma R $red, G $green, B $blue")
        }

        return Color.rgb(red, green, blue)
    }

    fun Bitmap.calculateBrightnessMean(): Int {
        val bitmap = this
        val height = bitmap.height
        val width = bitmap.width
        var total = 0

        for(y in 0 until height) {
            for(x in 0 until width) {
                val (red, green, blue) = bitmap.getPixel(x, y).extractRGB()
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





