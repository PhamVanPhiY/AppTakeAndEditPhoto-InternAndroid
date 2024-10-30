package com.example.appinternandroid

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    private var hasEdited = false
    private var editedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)

        imageView = findViewById(R.id.imageView)
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        val btnCrop = findViewById<Button>(R.id.btnCrop)
        val btnRotate = findViewById<Button>(R.id.btnRotate)
        val btnBlur = findViewById<Button>(R.id.btnBlur)
        saveButton = findViewById(R.id.btnSave)

        saveButton.isEnabled = false

        val imagePath = intent.getStringExtra("imagePath")
        if (imagePath != null) {
            val bitmap = getCorrectlyOrientedBitmap(imagePath)
            imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Lỗi: Đường dẫn ảnh không hợp lệ.", Toast.LENGTH_SHORT).show()
            finish()
        }

        cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                editedImageUri = UCrop.getOutput(result.data!!)
                if (editedImageUri != null) {
                    imageView.setImageURI(editedImageUri)
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
                Toast.makeText(this, "Lỗi: Đường dẫn ảnh không hợp lệ.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        btnRotate.setOnClickListener {
            enableSaveButton()
        }
        btnBlur.setOnClickListener {
            enableSaveButton()
        }

        saveButton.setOnClickListener {
            saveEditedImage()
        }
    }

    private fun enableSaveButton() {
        if (!hasEdited) {
            saveButton.isEnabled = true
            hasEdited = true
        }
    }

    private fun saveEditedImage() {
        val originalImagePath = intent.getStringExtra("imagePath") ?: return

        editedImageUri?.let { uri ->
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))

            // Tạo đối tượng File với đường dẫn ảnh gốc
            val originalFile = File(originalImagePath)

            // Ghi đè tệp gốc bằng ảnh đã chỉnh sửa
            FileOutputStream(originalFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            Toast.makeText(this, "Ảnh đã được lưu thành công!", Toast.LENGTH_SHORT).show()
            finish() // Đóng activity sau khi lưu
        } ?: run {
            Toast.makeText(this, "Lỗi: Không có ảnh để lưu.", Toast.LENGTH_SHORT).show()
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
}