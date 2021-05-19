package dev.polazzo.imageprocessing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CODE_PICK_IMAGE = 100
    private lateinit var imageViewOriginal: ImageView
    private lateinit var imageViewGray: ImageView
    private var lastPhotoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageViewOriginal = findViewById(R.id.imageView_original)
        imageViewGray = findViewById(R.id.imageView_gray)
    }

    fun onTapPickImage(view: View) {
        openGalleryForImage()
    }

    fun onTapTakePicture(view: View) {
        dispatchTakePictureIntent()
    }

    private fun setNewImage(newPhotoUri: Uri) {
        lastPhotoUri = newPhotoUri
        imageViewOriginal.setImageURI(newPhotoUri)
        imageViewGray.setImageURI(newPhotoUri)
        transformOriginalImageToGray(newPhotoUri)
    }

    private fun transformOriginalImageToGray(newPhotoUri: Uri) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0F)
        imageViewGray.colorFilter = ColorMatrixColorFilter(matrix)
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /// Pick image
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE && data?.data != null) {
            setNewImage(data.data!!) // handle chosen image
        }
        /// Take picture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && lastPhotoUri != null) {
            setNewImage(lastPhotoUri!!)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            setNewImage(toUri())
        }
    }
}
