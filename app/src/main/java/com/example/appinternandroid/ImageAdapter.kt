package com.example.appinternandroid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import java.io.File

class ImageAdapter(private val context: Context, private val imageFiles: List<File>) : BaseAdapter() {

    override fun getCount(): Int {
        return imageFiles.size
    }

    override fun getItem(position: Int): Any {
        return imageFiles[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_image, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        val imageFile = imageFiles[position]
        Glide.with(context).load(imageFile).into(imageView)

        return view
    }
}