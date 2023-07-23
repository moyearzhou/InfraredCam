package com.moyear.activity.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.moyear.core.Infrared
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

    fun deleteCapture(fileNameNoSuffix: String) {
        // 删除照片文件
        val result = galleryManager.deleteCapture(fileNameNoSuffix)
        if (!result) {
            return
        }

        // 删除当前照片后，通知正在预览的照片切换到下一张图片
        val index = indexOfCapture(fileNameNoSuffix)
//        Log.d(Constant.TAG_DEBUG, "Find the capture to delete in: $index")
        if (index >= 0) {
            list.removeAt(index)

            val newCapture = list[index]
            currentPreview.value = newCapture
        }

        // 更新相册列表
        galleryCaptures.value = list
    }

    fun indexOfCapture(captureName: String): Int {
        return if (captureName.endsWith(".jpg")) {
            list.indexOfFirst {
                it.name == captureName
            }
        } else {
            list.indexOfFirst {
                it.name == "$captureName.jpg"
            }
        }
    }
}