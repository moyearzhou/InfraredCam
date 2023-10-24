package com.moyear.activity.ui.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.moyear.Constant
import com.moyear.core.Infrared
import com.moyear.core.Infrared.Companion.CAPTURE_PHOTO
import com.moyear.core.Infrared.Companion.CAPTURE_VIDEO
import com.moyear.global.GalleryManager

class GalleryModel(application: Application) : AndroidViewModel(application) {

    private val galleryManager = GalleryManager.getInstance()

    private var list = mutableListOf<Infrared.CaptureInfo>()

    val galleryCaptures = MutableLiveData<List<Infrared.CaptureInfo>>()

    /**
     * 当前正在预览的照片
     */
    val currentPreview = MutableLiveData<Infrared.CaptureInfo>()

    fun updateGallery() {
        list.clear()
        list.addAll(galleryManager.listCaptures()
            .sortedBy { it.name }
            .reversed())
        galleryCaptures.value = list
    }

    fun deleteCapture(capture: Infrared.CaptureInfo) {
        var result = false
        if (capture.type == CAPTURE_PHOTO) {
//            val fileNameNoSuffix = capture.name.removeSuffix(".jpg")
            result = galleryManager.deleteCapture(capture)
        } else  if (capture.type == CAPTURE_VIDEO) {
            // 删除照片文件
            result = galleryManager.deleteRecord(capture)
        }

        if (!result) {
            Log.d(Constant.TAG_DEBUG, "Delete capture $capture false")
            return
        }

        // 删除当前照片后，通知正在预览的照片切换到下一张图片
        val index = list.indexOf(capture)
//        Log.d(Constant.TAG_DEBUG, "Find the capture to delete in: $index")
        if (index >= 0) {
            list.removeAt(index)

            val newCapture = list[index]
            currentPreview.value = newCapture

            // 更新相册列表
            galleryCaptures.value = list
        }
    }
}