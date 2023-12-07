package com.moyear.utils

import com.moyear.core.Infrared
import java.io.File

class CustomFileHelper {

    companion object {

        @JvmStatic
        fun listRawFrames(captureInfo: Infrared.CaptureInfo): List<File>? {
            if (captureInfo.type == Infrared.CAPTURE_PHOTO) return null

            val imageFiles = File(captureInfo.path)
                .listFiles()
                ?.filter {
                    it.extension != "json" && it.name != "thumb.jpg"
                }

            return imageFiles

        }

    }

}