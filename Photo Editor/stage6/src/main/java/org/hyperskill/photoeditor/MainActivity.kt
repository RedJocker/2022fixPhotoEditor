package org.hyperskill.photoeditor

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.slider.Slider
import kotlinx.coroutines.*

private val saveDir = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
private const val SAVE_EXTERNAL_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
private const val SAVE_EXTERNAL_REQUEST_CODE = 0

class MainActivity : AppCompatActivity() {
    private lateinit var currentImage: ImageView
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var btnGallery: Button
    private lateinit var save: Button
    private lateinit var slider: Slider
    private lateinit var slContrast: Slider
    private lateinit var slSaturation: Slider
    private lateinit var slGamma: Slider
    private var lastJob: Job? = null  // the field to keep track of the last job in case we wish to cancel it
    private var loadedImage: Bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565)
        get() {
            synchronized(field) {
                return field.copy(field.config, true)
            }
        }
        set(value) {
            synchronized(field) {
                field = value
            }
        }


    //private lateinit var binding: ActivityMainBinding
    private val imageResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    val bitmap = imageProcessor.createBitmap(it)
                    loadedImage = bitmap
                    currentImage.setImageBitmap(bitmap)
                }
            }
        }

    private fun callimageproc(bright: Float, contrast: Float, satur: Float, gamma: Float)  {

        if(lastJob != null) {
            lastJob?.cancel()
            println("lastJob canceled")
        }

        lastJob = GlobalScope.launch(Dispatchers.Default) {
            //  the execution inside this block is already asynchronous as you can see by the print below

            //  I/System.out: onSliderChanges job making calculations running on thread DefaultDispatcher-worker-1
            println("onSliderChanges " + "job making calculations running on thread ${Thread.currentThread().name}")

            // if the current image is null, we have nothing to do with it
            //val bitmap = currentOriginalImageDrawable?.bitmap ?: return@launch


            // if you need to make some computations and wait for the result, you can use the async block
            // it will schedule a new coroutine task and return a Deferred object that will have the
            // returned value
            val brightenCopyDeferred: Deferred<Bitmap> = this.async {
                return@async imageProcessor.changeBrightness(bright.toInt(), contrast.toInt(), satur.toInt(), gamma.toDouble(), loadedImage)
            }
            // here we wait for the result
            val newBitmap: Bitmap = brightenCopyDeferred.await()
            runOnUiThread {
                    // here we are already on the main thread, as you can see on the println below
                    //  I/System.out: onSliderChanges job updating view running on thread main
                    println("onSliderChanges " + "job updating view running on thread ${Thread.currentThread().name}")
                    currentImage.setImageBitmap(newBitmap)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        bindViews()

        //do not change this line
        val bitmap = createBitmap()
        currentImage.setImageBitmap(bitmap)
        loadedImage = bitmap
        imageProcessor = ImageProcessor(this)

        btnGallery.setOnClickListener {
            val selectImageIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            imageResultLauncher.launch(selectImageIntent)
        }

        save.setOnClickListener {
            onSaveClick()
        }



        slider.addOnChangeListener { _, _, _ ->
            callimageproc(slider.value, slContrast.value, slSaturation.value, slGamma.value)
        }

        slContrast.addOnChangeListener { _, _, _ ->

            callimageproc(slider.value, slContrast.value, slSaturation.value, slGamma.value)
        }

        slSaturation.addOnChangeListener { _, _, _ ->

            callimageproc(slider.value, slContrast.value, slSaturation.value, slGamma.value)
        }
        slGamma.addOnChangeListener { _, _, _ ->

            callimageproc(slider.value, slContrast.value, slSaturation.value, slGamma.value)
        }
    }

    private fun onSaveClick() {
        Permission(this, SAVE_EXTERNAL_PERMISSION).apply {
            check(object : Permission.Callback {
                override fun onHasPermission() {
                    saveBitmap(currentImage.drawable.toBitmap())
                }

                override fun onNoPermission() {
                    //requestPermissionLauncher.launch(saveExternalPermission)
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(SAVE_EXTERNAL_PERMISSION),
                        SAVE_EXTERNAL_REQUEST_CODE
                    )
                }
            })
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.width)
        values.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.height)

        val uri = this.contentResolver.insert(saveDir, values) ?: return

        contentResolver.openOutputStream(uri).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private fun bindViews() {
        currentImage = findViewById(R.id.ivPhoto)
        btnGallery = findViewById(R.id.btnGallery)
        save = findViewById(R.id.btnSave)
        slider = findViewById(R.id.slBrightness)
        slContrast = findViewById(R.id.slContrast)
        slSaturation = findViewById(R.id.slSaturation)
        slGamma = findViewById(R.id.slGamma)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SAVE_EXTERNAL_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSaveClick()
            }
        }
    }

    // do not change this function
    private fun createBitmap(): Bitmap {
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
                B = (x + y) % 100 + 120

                pixels[index] = Color.rgb(R, G, B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }
}