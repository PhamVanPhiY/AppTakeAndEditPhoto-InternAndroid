package com.example.appinternandroid

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.BlurMaskFilter
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream

class EditImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var cropImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var undoButton: Button
    private var hasEdited = false
    private var editedBitmap: Bitmap? = null
    private var originalBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)

        imageView = findViewById(R.id.imageView)
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        val btnCrop = findViewById<Button>(R.id.btnCrop)
        val btnContrast = findViewById<Button>(R.id.btnContrast)
        val btnBlur = findViewById<Button>(R.id.btnBlur)
        saveButton = findViewById(R.id.btnSave)
        deleteButton = findViewById(R.id.btnDelete)
        undoButton = findViewById(R.id.btnUndo)

        saveButton.isEnabled = false

        val imagePath = intent.getStringExtra("imagePath")
        if (imagePath != null) {
            val bitmap = getCorrectlyOrientedBitmap(imagePath)
            originalBitmap = bitmap?.copy(bitmap.config, true) // Save original image
            imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Error: Invalid image path", Toast.LENGTH_SHORT).show()
            finish()
        }

        cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val editedImageUri = UCrop.getOutput(result.data!!)
                if (editedImageUri != null) {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(editedImageUri))
                    imageView.setImageBitmap(bitmap)
                    editedBitmap = bitmap // Save cropped bitmap
                    enableSaveButton()
                }
            }
        }

        btnCrop.setOnClickListener {
            if (imagePath != null) {
                val sourceUri = Uri.fromFile(File(imagePath))
                val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
                val uCrop = UCrop.of(sourceUri, destinationUri)
                uCrop.withAspectRatio(1f, 1f)
                uCrop.withMaxResultSize(600, 600)
                cropImageLauncher.launch(uCrop.getIntent(this))
            } else {
                Toast.makeText(this, "Error: Invalid image path", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, PhotoListActivity::class.java)
            startActivity(intent)
        }

        btnContrast.setOnClickListener {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val contrastBitmap = adjustContrast(bitmap, 1.5f) // Adjust contrast value as desired
            imageView.setImageBitmap(contrastBitmap)
            editedBitmap = contrastBitmap // Save adjusted bitmap
            enableSaveButton()
        }

        btnBlur.setOnClickListener {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editedBitmap = blurBitmap(bitmap, 20f) // Save blurred bitmap
            imageView.setImageBitmap(editedBitmap)
            enableSaveButton()
        }

        saveButton.setOnClickListener {
            saveEditedImage()
        }

        deleteButton.setOnClickListener {
            imageView.setImageDrawable(null) // Clear the image from the ImageView
            editedBitmap = null // Reset the editedBitmap variable
            saveButton.isEnabled = false // Disable save button
            hasEdited = false

            val imagePath = intent.getStringExtra("imagePath")
            imagePath?.let {
                val file = File(it)
                if (file.exists()) {
                    file.delete() // Delete the file from storage
                    Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
                }
            }

            val intent = Intent(this, PhotoListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        undoButton.setOnClickListener {
            originalBitmap?.let {
                imageView.setImageBitmap(it) // Restore original image
                editedBitmap = it.copy(it.config, true) // Reset editedBitmap to original
                saveButton.isEnabled = false // Disable save button
                hasEdited = false
                Toast.makeText(this, "Original State of Image", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "No original image to restore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableSaveButton() {
        if (!hasEdited) {
            saveButton.isEnabled = true
            hasEdited = true
        }
    }

    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val contrastMatrix = android.graphics.ColorMatrix().apply {
            setScale(contrast, contrast, contrast, 1f)
        }
        val paint = Paint().apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(contrastMatrix)
        }
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return outputBitmap
    }

    private fun saveEditedImage() {
        val originalImagePath = intent.getStringExtra("imagePath") ?: return

        editedBitmap?.let { bitmap ->

            FileOutputStream(originalImagePath).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            Toast.makeText(this, "Image saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } ?: run {
            Toast.makeText(this, "Error: No image to save", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCorrectlyOrientedBitmap(imagePath: String): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun blurBitmap(bitmap: Bitmap, radius: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(outputBitmap)
        val paint = Paint()
        val blurFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        paint.maskFilter = blurFilter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return outputBitmap
    }
}
