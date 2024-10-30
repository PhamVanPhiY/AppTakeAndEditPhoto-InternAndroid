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
    private lateinit var gridView: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        gridView = findViewById(R.id.gridView)
        val toggleButton = findViewById<ImageButton>(R.id.toggleButton)
        val backButton = findViewById<ImageButton>(R.id.btnBack)

        loadImages()

        toggleButton.setOnClickListener {
            isTwoColumns = !isTwoColumns
            gridView.numColumns = if (isTwoColumns) 2 else 3
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadImages()
    }

    private fun loadImages() {
        val imagesDir = File("/storage/emulated/0/Android/data/com.example.appinternandroid/files/Pictures/Images")
        if (imagesDir.exists() && imagesDir.isDirectory) {
            val imageFiles = imagesDir.listFiles { file ->
                file.isFile && (file.extension == "jpg" || file.extension == "png")
            }?.toList() ?: emptyList()


            val sortedImages = imageFiles.sortedByDescending { it.lastModified() }

            if (sortedImages.isNotEmpty()) {
                val adapter = ImageAdapter(this, sortedImages)
                gridView.adapter = adapter
                adapter.notifyDataSetChanged()

                gridView.setOnItemClickListener { _, _, position, _ ->
                    val selectedImage = sortedImages[position]
                    selectedImage?.let {
                        val intent = Intent(this, EditImageActivity::class.java)
                        intent.putExtra("imagePath", it.absolutePath)
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(this, "Lỗi: Không thể mở ảnh.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Không có ảnh nào trong thư mục này.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Thư mục chứa ảnh không tồn tại.", Toast.LENGTH_SHORT).show()
        }
    }
}
