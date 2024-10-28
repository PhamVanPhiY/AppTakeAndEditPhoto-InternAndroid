package com.example.appinternandroid

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class EditImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)

        imageView = findViewById(R.id.imageView)
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        val btnCrop = findViewById<Button>(R.id.btnCrop)
        val btnRotate = findViewById<Button>(R.id.btnRotate)
        val btnBlur = findViewById<Button>(R.id.btnBlur)

        val imagePath = intent.getStringExtra("imagePath")
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Lỗi: Đường dẫn ảnh không hợp lệ.", Toast.LENGTH_SHORT).show()
            finish() // Đóng Activity nếu đường dẫn ảnh không hợp lệ
        }

        backButton.setOnClickListener {
            finish() // Quay lại Activity trước đó mà không tạo Intent mới
        }

        btnCrop.setOnClickListener { /* Thực hiện cắt ảnh */ }
        btnRotate.setOnClickListener { /* Thực hiện xoay ảnh */ }
        btnBlur.setOnClickListener { /* Thực hiện làm mờ ảnh */ }
    }
}
