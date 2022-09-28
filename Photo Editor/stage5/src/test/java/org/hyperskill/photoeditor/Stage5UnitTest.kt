package org.hyperskill.photoeditor

import android.graphics.drawable.BitmapDrawable
import android.os.Looper
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
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper

// version 1.5
@RunWith(RobolectricTestRunner::class)
class Stage5UnitTest {

    private val messageNullAfterFilters = "Image was null after filters been applied"
    private val messageWrongValues = "Wrong values after filters been applied."
    private val marginError = 3

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    private val activity = activityController.setup().get()
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
    private val slContrast by lazy { activity.findViewByString<Slider>("slContrast")
        .also { testShouldCheckSlider(it, "slContrast") }
    }
    private val slSaturation by lazy { activity.findViewByString<Slider>("slSaturation")
        .also { testShouldCheckSlider(it, "slSaturation") }
    }
    private val slGamma by lazy { activity.findViewByString<Slider>("slGamma")
        .also { testShouldCheckSlider(it, "slGamma",
            0.2f, 0.2f, 4f, 1f)
        }
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
    fun testShouldCheckSliderContrast() {
        slContrast // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderSaturation() {
        slSaturation // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderGamma() {
        slGamma // initializes variable and perform initialization assertions
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
    fun testShouldCheckSliderSaturationNotCrashingByDefault() {
        ivPhoto
        slSaturation.value += slSaturation.stepSize
        slSaturation.value -= slSaturation.stepSize
        shadowLooper.runToEndOfTasks()
        (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
    }

    @Test
    fun testShouldCheckSliderGammaNotCrashingByDefault() {
        ivPhoto
        slGamma.value += slGamma.stepSize
        slGamma.value -= slGamma.stepSize
        shadowLooper.runToEndOfTasks()
        (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
    }

    @Test
    fun testShouldCheckHighBrightnessValue() {
        slBrightness
        ivPhoto
        val expectedRgb = Triple(230, 255, 255)
        slBrightness.value += 120

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeContrastValue() {
        slContrast
        ivPhoto
        val expectedRgb = Triple(85, 154, 177)

        slContrast.value += 100

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeSaturationValue() {
        slSaturation
        ivPhoto
        val expectedRgb = Triple(88, 146, 165)

        slSaturation.value += 80

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeGammaValue() {
        slGamma
        ivPhoto
        val expectedRgb = Triple(56, 86, 98)

        slGamma.value += 4 * slGamma.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }


    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        slBrightness
        slContrast
        slSaturation
        slGamma
        ivPhoto
        val expectedRgb = Triple(36, 208, 246)


        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 4
        slContrast.value += slContrast.stepSize
        slSaturation.value += slSaturation.stepSize * 10
        slSaturation.value += slSaturation.stepSize * 5
        slGamma.value -= slGamma.stepSize * 2

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit2() {
        slBrightness
        slContrast
        slSaturation
        slGamma
        ivPhoto
        val expectedRgb = Triple(71, 122, 186)

        slGamma.value += slGamma.stepSize * 5
        slSaturation.value += slSaturation.stepSize * 5
        slBrightness.value += slBrightness.stepSize
        slContrast.value -= slContrast.stepSize
        slBrightness.value += slBrightness.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 90, 80)
        assertColorsValues("$messageWrongValues For x=90, y=80", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEditWiderSample() {
        slBrightness
        slContrast
        slSaturation
        slGamma
        ivPhoto

        val sample = listOf(
            15 to 41,
            30 to 98,
            150 to 42,
            97 to 20,
            11 to 50,
            191 to 4,
            5 to 62,
            73 to 90,
            1 to 0,
            39 to 80
        )
        val expected = listOf(
            Triple(0, 180, 255),
            Triple(0, 255, 242),
            Triple(0, 99, 255),
            Triple(223, 0, 223),
            Triple(0, 206, 255),
            Triple(143, 0, 255),
            Triple(0, 240, 255),
            Triple(0, 255, 255),
            Triple(0, 110, 255),
            Triple(0, 255, 225)
        )


        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 4
        slContrast.value += slContrast.stepSize
        slSaturation.value += slSaturation.stepSize * 10
        slSaturation.value += slSaturation.stepSize * 5
        slGamma.value -= slGamma.stepSize * 2

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        for(i in sample.indices) {
            val point = sample[i]
            val actual = singleColor(actualImage, point.first, point.second)
            assertColorsValues("$messageWrongValues For x=${point.first}, y=${point.second}",
                expected[i], actual, marginError
            )
        }
    }
}