package org.hyperskill.photoeditor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var currentImage: ImageView

    private val galleryButton: Button by lazy {
        findViewById<Button>(R.id.btnGallery)  // renaming btnGallery should produce "View with id "btnGallery" was not found" obs: use refactor->rename (fn + shift + f6)
//            .also { it.text = "wrongText" }    // uncommenting should produce "Wrong text for btnGallery expected:<[GALLERY]> but was:<[WRONGTEXT]>"
    }


    private val activityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                currentImage.setImageURI(uri)
//                currentImage.setImageBitmap(null)                        //  should produce "Image was null after loading from gallery"
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        setListener()

        //do not change this line
        currentImage.setImageBitmap(createBitmap())      // commenting out this line should produce "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
//        currentImage.setImageBitmap(createBitmap().scale(10, 100))  // should produce "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
//        currentImage.setImageBitmap(createBitmap().scale(200, 10))  // should produce "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
//        currentImage.setImageBitmap(BitmapFactory.decodeResource(this.resources, R.drawable.myexample).scale(200, 100))  // should produce "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
        //
    }


    private fun setListener() {
        galleryButton.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //val intent = Intent(Intent.ACTION_CHOOSER, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)   // changing above line to this should produce "Intent found was different from expected"

            activityResultLauncher.launch(intent)    // commenting this line should produce "No intent was found by tests. Have you launched an intent?"
        }
    }

    private fun bindViews() {
        currentImage = findViewById(R.id.ivPhoto)
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
//                pixels[index] = Color.rgb(R + 20,G,B) // should produce "Is defaultBitmap set correctly? Rgb expected:<(110, 140, 150)> but was:<(__, __, __)>"

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }
}