package org.hyperskill.photoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import android.provider.MediaStore.Images

import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.core.graphics.scale

import org.hyperskill.photoeditor.BitmapFilters.brightenCopy
import org.hyperskill.photoeditor.BitmapFilters.calculateBrightnessMean
import org.hyperskill.photoeditor.BitmapFilters.contrastedCopy


class MainActivity : AppCompatActivity() {

    private val currentImage: ImageView by lazy {
        findViewById<ImageView>(R.id.ivPhoto)
    }

    private val galleryButton: Button by lazy {
        findViewById<Button>(R.id.btnGallery)
//                    .also { it.text = "wrongText" }    // should produce "Wrong text for btnGallery expected:<[GALLERY]> but was:<[WRONGTEXT]>"
    }

    private val saveButton : Button by lazy {
        findViewById<Button>(R.id.btnSave)
//            .also{ it.text = "wrong value" }  // should produce "Wrong text for btnSave expected:<[SAVE]> but was:<[WRONG VALUE]>"
    }

    private val brightnessSlider: Slider by lazy {
        findViewById<Slider>(R.id.slBrightness)
//            .also {
//                it.stepSize = 0.2f       // should produce ""slBrightness" should have proper stepSize attribute expected:<10.0> but was:<0.2>"
//                it.valueFrom = 0f       // should produce ""slBrightness" should have proper valueFrom attribute expected:<-250.0> but was:<0.0>"
//                it.valueTo = 500f      // should produce ""slBrightness" should have proper valueTo attribute expected:<250.0> but was:<500.0>"
//                it.value = 100f       // should produce ""slBrightness" should have proper initial value expected:<0.0> but was:<100.0>"
//            }
    }

    private val contrastSlider: Slider by lazy {
        findViewById<Slider>(R.id.slContrast)
//            .also {
//                it.stepSize = 0.5f        // should produce ""slContrast" should have proper stepSize attribute expected:<10.0> but was:<0.5>"
//                it.valueFrom = -100.0f   // should produce ""slContrast" should have proper valueFrom attribute expected:<-250.0> but was:<-100.0>"
//                it.valueTo = 100.0f     // should produce ""slContrast" should have proper valueTo attribute expected:<250.0> but was:<100.0>"
//                it.value = 50.0f       // should produce ""slContrast" should have proper initial value expected:<0.0> but was:<50.0>"
//            }
    }

    private val intentLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                currentImage.setImageURI(uri)
                currentOriginalImageDrawable = currentImage.drawable as BitmapDrawable
            }
        }

    private var currentOriginalImageDrawable: BitmapDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListener()

        //do not change this line
        currentImage.setImageBitmap(createBitmap())      // commenting out this line should produce "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
//        currentImage.setImageBitmap(createBitmap().scale(10, 100))  // should produce "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
//        currentImage.setImageBitmap(createBitmap().scale(200, 10))  // should produce "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
//        currentImage.setImageBitmap(BitmapFactory.decodeResource(this.resources, R.drawable.myexample).scale(200, 100))  // should produce "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
        //

        currentOriginalImageDrawable = currentImage.drawable as BitmapDrawable?
    }


    private fun setListener() {
        galleryButton.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentLauncher.launch(intent)
        }

        saveButton.setOnClickListener { _ ->
            if(hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val bitmap = ((currentImage.drawable as BitmapDrawable?)?.bitmap ?: return@setOnClickListener)

                val values = ContentValues()

                values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
                values.put(Images.Media.MIME_TYPE, "image/jpeg")
                values.put(Images.ImageColumns.WIDTH, bitmap.width)
                values.put(Images.ImageColumns.HEIGHT, bitmap.height)

                val uri = this@MainActivity.contentResolver.insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return@setOnClickListener

                val openOutputStream = contentResolver.openOutputStream(uri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, openOutputStream)
            } else {
                requestPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }
/*
          //case when brightnessSlider ignores contrastSlider values
         //should produce error "Wrong values after filters been applied.  Order of slider events should not matter. For x=90, y=80 expected: <(141, 210, 255)> actual: <(140, 170, 200)>"
        brightnessSlider.addOnChangeListener { slider, sliderValue, fromUser ->
            val bitmap = currentOriginalImageDrawable?.bitmap ?: return@addOnChangeListener
            val brightnessValue = sliderValue.toInt()
            val brightenCopy = bitmap.brightenCopy(brightnessValue)

            currentImage.setImageBitmap(brightenCopy)
        }

        contrastSlider.addOnChangeListener { slider, sliderValue, fromUser ->
            val bitmap = currentOriginalImageDrawable?.bitmap ?: return@addOnChangeListener


            val brightnessValue = brightnessSlider.value.toInt()
            val brightenCopy = bitmap.brightenCopy(brightnessValue)

            val contrastValue = sliderValue.toInt()
//            val averageBrightness = bitmap.calculateBrightnessMean()                   // average with values before brightness filter
            val averageBrightness = brightenCopy.calculateBrightnessMean()              // average with values after brightness filter
            val contrastedCopy = brightenCopy.contrastedCopy(contrastValue, averageBrightness)

            currentImage.setImageBitmap(contrastedCopy)
        }

        // comment both .addOnChangeListener below to test case above       */
        brightnessSlider.addOnChangeListener(this::onSliderChanges)
        contrastSlider.addOnChangeListener(this::onSliderChanges)
    }

    private fun onSliderChanges(slider: Slider, sliderValue: Float, fromUser: Boolean) {
        val bitmap = currentOriginalImageDrawable?.bitmap ?: return

        val brightnessValue = brightnessSlider.value.toInt()
        val brightenCopy = bitmap.brightenCopy(brightnessValue)

        val contrastValue = contrastSlider.value.toInt()
//        val averageBrightness = bitmap.calculateBrightnessMean()          // average with values before brightness filter, should produce "Wrong values after filters been applied. For x=_, y=_ expected: <(__, __, __)> actual: <(__, __, __)>"
        val averageBrightness = brightenCopy.calculateBrightnessMean()     // average with values after brightness filter
        val contrastedCopy = brightenCopy.contrastedCopy(contrastValue, averageBrightness)

        currentImage.setImageBitmap(contrastedCopy)
//        currentImage.setImageBitmap(null)  // should produce "Image was null after filters been applied"
    }



    private fun hasPermission(manifestPermission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_GRANTED
        } else {
            PermissionChecker.checkSelfPermission(this, manifestPermission) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(permissionsToRequest: List<String>) {

        permissionsToRequest.filter { permissionToRequest ->
            hasPermission(permissionToRequest).not()
        }.also {
            if(it.isEmpty().not()) {
                // asking runtime permission is only for M or above. Before M permissions are
                // required on installation based on AndroidManifest.xml, so in theory it should
                // have required permissions if it is running
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i("Permission", "requestPermissions")
                    this.requestPermissions(it.toTypedArray(), 0)
                } else {
                    // this should happen only if permission not requested on AndroidManifest.xml
                    Log.i("Permission", "missing required permission")
                }
            } else {
                Log.i("Permission",  "All required permissions are granted")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEachIndexed { index: Int, result: Int ->
            if (result == PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionRequest", "${permissions[index]} granted")
                if(permissions[index] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    galleryButton.callOnClick()
                } else if(permissions[index] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    saveButton.callOnClick()
                }
            } else {
                Log.i("PermissionRequest", "${permissions[index]} denied")
            }
        }
    }

    // do not change this function
    fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)
        // get pixel array from source

        var R: Int
        var G: Int
        var B: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {
                // get current index in 2D-matrix
                index = y * width + x
                // get color
                R = x % 100 + 40
                G = y % 100 + 80
                B = (x+y) % 100 + 120

                pixels[index] = Color.rgb(R,G,B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }
}




