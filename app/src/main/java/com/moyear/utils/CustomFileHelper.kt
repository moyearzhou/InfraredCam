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

        @JvmStatic
        fun isRawVideos(file: File): Boolean {
            if (file.isFile || !file.name.endsWith(".video")) return false

            val configFile = File(file, "config.json")
            if (!configFile.exists()) return false

            return true
        }

        fun getRawVideoFile(captureInfo: Infrared.CaptureInfo): File? {
            if (captureInfo.type == Infrared.CAPTURE_PHOTO) return null;

            val captureFile = File(captureInfo.path)

            val rawFileName = captureFile.nameWithoutExtension + ".raws"

            return File(captureFile.parent, rawFileName)

        }

    }

}