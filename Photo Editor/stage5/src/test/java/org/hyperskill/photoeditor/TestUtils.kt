package org.hyperskill.photoeditor

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.View
import org.junit.Assert
import kotlin.math.abs

// version 1.5
object TestUtils {

    inline fun <reified T> Activity.findViewByString(idString: String): T {
        val id = this.resources.getIdentifier(idString, "id", this.packageName)
        val view: View? = this.findViewById(id)

        val idNotFoundMessage = "View with id \"$idString\" was not found"
        val wrongClassMessage = "View with id \"$idString\" is not from expected class. " +
                "Expected ${T::class.java.simpleName} found ${view?.javaClass?.simpleName}"

        Assert.assertNotNull(idNotFoundMessage, view)
        Assert.assertTrue(wrongClassMessage, view is T)

        return view as T
    }

    fun assertColorsValues(message: String, expected: Triple<Int, Int, Int>, actual: Triple<Int, Int, Int>, marginError: Int) {
        val messageWrongValuesFormat = "%s expected: <(%d, %d, %d)> actual: <(%d, %d, %d)>"
        val (expectedRed, expectedGreen, expectedBlue) = expected
        val (actualRed, actualGreen, actualBlue) = actual

        val messageWrongValues = messageWrongValuesFormat.format( message,
            expectedRed, expectedGreen, expectedBlue,
            actualRed, actualGreen, actualBlue
        )

        Assert.assertTrue(messageWrongValues, abs(expectedRed - actualRed) <= marginError)
        Assert.assertTrue(messageWrongValues, abs(expectedGreen - actualGreen) <= marginError)
        Assert.assertTrue(messageWrongValues, abs(expectedBlue - actualBlue) <= marginError)
    }

    fun createGalleryPickActivityResultStub(activity: MainActivity): Intent {
        val resultIntent = Intent()
        val uri = getUriToDrawable(activity,R.drawable.myexample)
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
}
