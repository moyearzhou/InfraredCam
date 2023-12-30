package com.moyear.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.bumptech.glide.Glide
import com.moyear.R
import com.moyear.core.Infrared
import java.io.File

class GalleryListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Infrared.CaptureInfo>()

    var onClickCallBack: OnClickCallBack ?= null

    private var span: Int = 3

    private var thumbnailSize = -1

    private var intervalSize = dp2px(5f)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_capture, parent, false)

        if (thumbnailSize < 0) {
            thumbnailSize = (parent.width - (span - 1) * intervalSize ) / span
        }
        return CaptureHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as CaptureHolder
        val capture = data[position]

        viewHolder.itemView.setOnClickListener {
            onClickCallBack?.onClick(capture)
        }

        // 正方形样式显示
        val param = viewHolder.imgCapture.layoutParams
        param.height = thumbnailSize
        param.width = thumbnailSize
        viewHolder.imgCapture.layoutParams = param

        viewHolder.imgPlay.let {
            if (capture.type == Infrared.CAPTURE_PHOTO) {
                it.visibility = View.GONE

            } else if (capture.type == Infrared.CAPTURE_VIDEO) {
                it.visibility = View.VISIBLE
            }
        }


        val imgFile = Infrared.findCaptureImageFile(capture)
        imgFile?.let {
            if (!it.exists()) return@let

            Glide.with(viewHolder.itemView)
                .load(it)
                .centerCrop()
                .into(viewHolder.imgCapture)
        }
    }


    fun updateData(list: List<Infrared.CaptureInfo>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    class CaptureHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapture: ImageView = itemView.findViewById(R.id.img_capture)
        val imgPlay: ImageView = itemView.findViewById(R.id.img_play)

    }

    interface OnClickCallBack {
        fun onClick(captureInfo: Infrared.CaptureInfo)
    }

}