package com.moyear.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.moyear.R
import com.moyear.core.Infrared
import java.io.File

class GalleryPreviewAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val gallery = mutableListOf<Infrared.CaptureInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =  LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_preview, parent, false)
        return PreviewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gallery.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val capture = gallery[position]

        val imgFile = File(capture.path)

        val viewHolder = holder as PreviewHolder
        Glide.with(holder.itemView)
            .load(imgFile)
            .into(viewHolder.imgPreview)
    }

    fun updateData(list: List<Infrared.CaptureInfo>) {
        gallery.clear()
        gallery.addAll(list)
        notifyDataSetChanged()
    }

    class PreviewHolder(itemView: View) : ViewHolder(itemView) {
        val imgPreview: ImageView = itemView.findViewById(R.id.img_preview)
    }

}