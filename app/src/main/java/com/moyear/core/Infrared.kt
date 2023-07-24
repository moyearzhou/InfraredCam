package com.moyear.core

import android.graphics.Bitmap
import android.view.View
import java.io.File

class Infrared {

    companion object {
        const val CAPTURE_PHOTO = 0
        const val CAPTURE_VIDEO = 1

        @JvmStatic
        fun findCaptureImageFile(capture: CaptureInfo): File? {
            var imgFile: File ?= null

            if (capture.type == Infrared.CAPTURE_PHOTO) {
                imgFile = File(capture.path)

            } else if (capture.type == Infrared.CAPTURE_VIDEO) {
                val videoFile = File(capture.path)
                imgFile = File(videoFile, "thumb.jpg")
            }

            return imgFile

        }
    }



    /**
     * 采集的照片或者视频
     */
    data class CaptureInfo(var name: String, var path: String, var type: Int = CAPTURE_PHOTO) {}

}