package org.hyperskill.photoeditor.internals

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Button
import android.widget.ImageView
import org.hyperskill.photoeditor.R
import org.junit.Assert
import org.junit.Assert.assertTrue
import kotlin.math.abs

open class PhotoEditorUnitTest<T : Activity>(clazz: Class<T>): AbstractUnitTest<T>(clazz) {

    val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto")
        .also(this::assertImageIsSetToDefaultBitmap)
    }

    val btnGallery by lazy { activity.findViewByString<Button>("btnGallery")
        .also { assertButtonName(it, "GALLERY", "btnGallery") }
    }

    fun assertColorsValues(message: String, expected: Triple<Int, Int, Int>, actual: Triple<Int, Int, Int>, marginError: Int) {
        val messageWrongValuesFormat = "%s expected: <(%d, %d, %d)> actual: <(%d, %d, %d)>"
        val (expectedRed, expectedGreen, expectedBlue) = expected
        val (actualRed, actualGreen, actualBlue) = actual

        val messageWrongValues = messageWrongValuesFormat.format( message,
            expectedRed, expectedGreen, expectedBlue,
            actualRed, actualGreen, actualBlue
        )

        assertTrue(messageWrongValues, abs(expectedRed - actualRed) <= marginError)
        assertTrue(messageWrongValues, abs(expectedGreen - actualGreen) <= marginError)
        assertTrue(messageWrongValues, abs(expectedBlue - actualBlue) <= marginError)
    }

    fun createGalleryPickActivityResultStub(activity: Activity): Intent {
        val resultIntent = Intent()
        val uri = getUriToDrawable(activity, R.drawable.myexample)
        resultIntent.data = uri
        return resultIntent
    }

    fun getUriToDrawable(context: Context, drawableId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.resources.getResourcePackageName(drawableId)
                    + '/' + context.resources.getResourceTypeName(drawableId)
                    + '/' + context.resources.getResourceEntryName(drawableId)
        )
    }

    fun singleColor(source: Bitmap, x: Int = 70, y: Int = 60): Triple<Int, Int, Int> {
        val pixel = source.getPixel(x, y)

        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        return  Triple(red,green,blue)
    }

    fun assertImageIsSetToDefaultBitmap(ivPhoto: ImageView) {
        val messageInitialImageNull = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
        val messageWrongInitialImage = "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
        val actualBitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageInitialImageNull
        )
        assertTrue(messageWrongInitialImage, 200 == actualBitmap.width)
        assertTrue(messageWrongInitialImage, 100 == actualBitmap.height)
        val expectedRgb = Triple(110, 140, 150)
        assertTrue(messageWrongInitialImage, expectedRgb == singleColor(actualBitmap))
    }

    fun assertButtonName(btn: Button, expectedInitialText: String, btnName: String) {
        Assert.assertEquals("Wrong text for $btnName",
            expectedInitialText.uppercase(), btn.text.toString().uppercase()
        )
    }
}