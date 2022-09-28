package org.hyperskill.photoeditor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.slider.Slider
import org.hyperskill.photoeditor.TestUtils.assertColorsValues
import org.hyperskill.photoeditor.TestUtils.findViewByString
import org.hyperskill.photoeditor.TestUtils.singleColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import kotlin.math.max
import kotlin.math.min

// version 1.5
@RunWith(RobolectricTestRunner::class)
class Stage3UnitTest {
    private val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
    private val messageWrongValues = "Wrong values after brightness applied."
    private val marginError = 1

    private val activityController: ActivityController<MainActivity> = Robolectric.buildActivity(MainActivity::class.java)
    private val activity: MainActivity = activityController.setup().get()
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto")
        .also(this::testShouldCheckImageIsSetToDefaultBitmap)
    }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery")
        .also { testShouldCheckButton(it, "GALLERY", "btnGallery") }
    }
    private val btnSave by lazy { activity.findViewByString<Button>("btnSave")
        .also { testShouldCheckButton(it, "SAVE", "btnSave") }
    }
    private val slBrightness by lazy { activity.findViewByString<Slider>("slBrightness")
        .also { testShouldCheckSlider(it, "slBrightness") }
    }


    private fun testShouldCheckImageIsSetToDefaultBitmap(ivPhoto: ImageView) {
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

    private fun testShouldCheckButton(btn: Button, expectedInitialText: String, btnName: String) {
        assertEquals("Wrong text for $btnName",
            expectedInitialText.uppercase(), btn.text.toString().uppercase()
        )
    }

    private fun testShouldCheckSlider(
        slBrightness: Slider, sliderName: String, expectedStepSize: Float = 10f ,
        expectedValueFrom: Float = -250f, expectedValueTo: Float = 250f, expectedValue: Float = 0f) {

        val message1 = "\"$sliderName\" should have proper stepSize attribute"
        assertEquals(message1, expectedStepSize, slBrightness.stepSize)

        val message2 = "\"$sliderName\" should have proper valueFrom attribute"
        assertEquals(message2, expectedValueFrom, slBrightness.valueFrom)

        val message3 = "\"$sliderName\" should have proper valueTo attribute"
        assertEquals(message3, expectedValueTo, slBrightness.valueTo)

        val message4 = "\"$sliderName\" should have proper initial value"
        assertEquals(message4, expectedValue, slBrightness.value)
    }

    @Test
    fun testShouldCheckImageView() {
        ivPhoto // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderBrightness() {
        slBrightness // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckButtonGallery() {
        btnGallery // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckButtonSave() {
        btnSave  // initializes variable and perform initialization assertions
    }

    @Test
    @Config(shadows = [CustomShadowBitmap::class])
    fun checkBitmapIsSaved() {
        CustomShadowBitmap.LastCompressed.init()
        val expectedUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
        val expectedFormat = Bitmap.CompressFormat.JPEG
        val expectedQuality = 100
        val expectedBitmap = (ivPhoto.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.RGB_565, false)
        val shadowContentResolver = shadowOf(activity.contentResolver)

        shadowActivity.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        btnSave.performClick()

        val messageWrongUri = "The uri for saving the image is wrong"
        val actualUri = shadowContentResolver.insertStatements.last().uri
        assertEquals(messageWrongUri, expectedUri, actualUri)

        val messageWrongFormat = "The image saved had wrong format"
        val actualFormat = CustomShadowBitmap.LastCompressed.compressedFormat
        assertEquals(messageWrongFormat, expectedFormat, actualFormat)

        val messageWrongQuality = "The image saved had wrong quality"
        val actualQuality = CustomShadowBitmap.LastCompressed.compressedQuality
        assertEquals(messageWrongQuality, expectedQuality, actualQuality)

        val messageWrongBitmap =
            "Image saved is not the same as the image that was displaying before the click"
        val actualBitmap = CustomShadowBitmap.LastCompressed.compressedBitmap
        assertTrue(messageWrongBitmap, expectedBitmap.sameAs(actualBitmap))
    }

    @Test
    @Config(shadows = [CustomShadowBitmap::class])
    fun checkBitmapIsSavedAfterPermissionIsGranted() {
        CustomShadowBitmap.LastCompressed.init()
        checkPermissionWasAsked()

        val expectedUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
        val expectedFormat = Bitmap.CompressFormat.JPEG
        val expectedQuality = 100
        val expectedBitmap = (ivPhoto.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.RGB_565, false)
        val shadowContentResolver = shadowOf(activity.contentResolver)

        shadowActivity.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        activity.onRequestPermissionsResult(
            0,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), arrayOf(
                PackageManager.PERMISSION_GRANTED).toIntArray()
        )
        shadowLooper.runToEndOfTasks()

        val messageAfterPermissionShouldSaveImage =
            "After the permission is granted the image should be saved without " +
                    "requiring additional clicks"
        val messageWrongUri = "The uri for saving the image is wrong"
        val actualUri = shadowContentResolver.insertStatements.lastOrNull()?.uri
            ?: throw AssertionError(messageAfterPermissionShouldSaveImage)
        assertEquals(messageWrongUri, expectedUri, actualUri)

        val messageWrongFormat = "The image saved had wrong format"
        val actualFormat = CustomShadowBitmap.LastCompressed.compressedFormat
        assertEquals(messageWrongFormat, expectedFormat, actualFormat)

        val messageWrongQuality = "The image saved had wrong quality"
        val actualQuality = CustomShadowBitmap.LastCompressed.compressedQuality
        assertEquals(messageWrongQuality, expectedQuality, actualQuality)

        val messageWrongBitmap = "Bitmap saved is not the same as the bitmap that was displaying before the click"
        val actualBitmap = CustomShadowBitmap.LastCompressed.compressedBitmap
        assertTrue(messageWrongBitmap, expectedBitmap.sameAs(actualBitmap))
    }

    @Test
    fun checkPermissionWasAsked () {
        btnSave
        btnSave.performClick()
        shadowLooper.runToEndOfTasks()

        val messagePermissionRequired = "Have you asked permission to write?"
        val permissionRequest = shadowActivity.lastRequestedPermission ?: throw AssertionError(messagePermissionRequired)

        val hasRequestedPermission =
            permissionRequest.requestedPermissions.any { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
        assert(hasRequestedPermission) { messagePermissionRequired }

        val actualRequestCode = permissionRequest.requestCode
        val expectedRequestCode = 0
        val messageWrongRequestCode =
            "Did you use the requestCode stated on description while requiring permissions?"
        assertEquals(messageWrongRequestCode, expectedRequestCode, actualRequestCode)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        slBrightness
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap // null checked on initialization
        val (initialRed, initialGreen, initialBlue) = singleColor(initialImage)

        val expectedRgb1 =
            Triple(initialRed + 110, initialGreen + 110, initialBlue + 105)
        val expectedRgb2 =
            Triple(initialRed - 110, initialGreen - 120, initialBlue - 120)

        slBrightness.value += slBrightness.stepSize * 5
        slBrightness.value += slBrightness.stepSize * 6
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb1 = singleColor(actualImage1)
        assertColorsValues(messageWrongValues, expectedRgb1, actualRgb1, marginError)

        slBrightness.value -= slBrightness.stepSize * 10
        slBrightness.value -= slBrightness.stepSize * 13
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage2 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb2 = singleColor(actualImage2)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb2, actualRgb2, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEditExhaustive() {
        slBrightness
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap // null checked on initialization

        slBrightness.value += slBrightness.stepSize * 15
        slBrightness.value -= slBrightness.stepSize * 4
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)

        for (x in 0 until initialImage.width) {
            for (y in 0 until initialImage.height) {
                val (initialRed, initialGreen, initialBlue) = singleColor(initialImage, x, y)
                val expectedRed = max(0, min(initialRed + 110, 255))
                val expectedGreen = max(0, min(initialGreen + 110, 255))
                val expectedBlue = max(0, min(initialBlue + 110, 255))
                val expectedRgb1 = Triple(expectedRed, expectedGreen, expectedBlue)
                val actualRgb1 = singleColor(actualImage1, x, y)
                assertColorsValues("$messageWrongValues For x=$x, y=$y", expectedRgb1, actualRgb1, marginError)
            }
        }
    }
}