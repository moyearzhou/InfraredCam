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
import com.moyear.global.MyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryModel(application: Application) : AndroidViewModel(application) {

    private val galleryManager = GalleryManager.getInstance()

    private var list = mutableListOf<Infrared.CaptureInfo>()

    val galleryCaptures = MutableLiveData<List<Infrared.CaptureInfo>>()

    /**
     * 当前正在预览的照片
     */
    val currentPreview = MutableLiveData<Infrared.CaptureInfo?>()

    fun updateGallery() {
        list.clear()
        list.addAll(galleryManager.listCaptures()
            .sortedBy { it.name }
            .reversed())
        galleryCaptures.value = list
    }

    private fun removeIndexAt(index: Int) {
        if (index < 0 || index >= list.size) {
            MyLog.e("Wrong index: $index")
            return
        }

        list.removeAt(index)

        var newCapture: Infrared.CaptureInfo? = null

        if (index < list.size) {
            // 如果后面一个不为空则显示后面一张
            newCapture = list[index]
            currentPreview.postValue(newCapture)
        } else if (index >= 1 && index - 1 < list.size) {
            // 如果后面为空，但是前面一个不为空则显示前面一张
            newCapture = list[index - 1]
        }

        currentPreview.postValue(newCapture)

        // 更新相册列表
        galleryCaptures.postValue(list)
    }

    fun deleteCapture(capture: Infrared.CaptureInfo) {
        // todo 删除大量文件夹需要耗时，使用携程操作
        CoroutineScope(Dispatchers.IO).launch {
            var result = false

            if (capture.type == CAPTURE_PHOTO) {
                result = galleryManager.deleteCapture(capture)
            } else  if (capture.type == CAPTURE_VIDEO) {
                // 删除照片文件
                result = galleryManager.deleteRecord(capture)
            }

            if (!result) {
                MyLog.d("Delete capture $capture false")
                return@launch
            }

            // 删除当前照片后，通知正在预览的照片切换到下一张图片
            val index = list.indexOf(capture)
            MyLog.d("Find the capture to delete in: $index")
            if (index >= 0) {
                removeIndexAt(index)
            }
        }
    }
}