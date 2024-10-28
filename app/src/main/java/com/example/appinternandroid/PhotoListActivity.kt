package com.example.appinternandroid

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PhotoListActivity : AppCompatActivity() {

    private var isTwoColumns = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        val gridView = findViewById<GridView>(R.id.gridView)
        val toggleButton = findViewById<ImageButton>(R.id.toggleButton)
        val backButton = findViewById<ImageButton>(R.id.btnBack)


        val imagesDir = File("/storage/emulated/0/Android/data/com.example.appinternandroid/files/Pictures/Images")
        if (imagesDir.exists() && imagesDir.isDirectory) {
            val imageFiles = imagesDir.listFiles { file ->
                file.isFile && (file.extension == "jpg" || file.extension == "png")
            }?.toList() ?: emptyList()

            if (imageFiles.isNotEmpty()) {
                val adapter = ImageAdapter(this, imageFiles)
                gridView.adapter = adapter


                gridView.setOnItemClickListener { _, _, position, _ ->
                    val selectedImage = imageFiles[position]
                    selectedImage?.let {
                        val intent = Intent(this, EditImageActivity::class.java)
                        intent.putExtra("imagePath", it.absolutePath)
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(this, "Lỗi: Không thể mở ảnh.", Toast.LENGTH_SHORT).show()
                    }
                }


                toggleButton.setOnClickListener {
                    isTwoColumns = !isTwoColumns
                    gridView.numColumns = if (isTwoColumns) 2 else 3
                }
                backButton.setOnClickListener{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }

            } else {
                Toast.makeText(this, "Không có ảnh nào trong thư mục này.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Thư mục chứa ảnh không tồn tại.", Toast.LENGTH_SHORT).show()
        }
    }
}
