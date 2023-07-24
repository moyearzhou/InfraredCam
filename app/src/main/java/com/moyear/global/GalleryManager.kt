package com.moyear.global

import android.util.Log
import com.moyear.Constant
import com.moyear.core.Infrared
import java.io.File

class GalleryManager {

    private constructor()

    companion object {
        private var INSTANCE: GalleryManager ?= null

        fun getInstance(): GalleryManager {
            if (INSTANCE == null) {
                INSTANCE = GalleryManager()
            }
            return INSTANCE!!
        }
    }

    /**
     * 列出所有的照片，按照名称排序
     */
    fun listCaptures(): List<Infrared.CaptureInfo> {
        val captures = mutableListOf<Infrared.CaptureInfo>()
        captures.addAll(listPhotoCaptures())
        captures.addAll(listVideoCaptures())

        return captures.sortedBy { it.name }
    }

    private fun listPhotoCaptures(): List<Infrared.CaptureInfo> {
        val photoDir = AppPath.getPhotoDir()

        if (photoDir.isFile || !photoDir.exists()) return emptyList()

        val files = photoDir.listFiles()

        if (files.isNullOrEmpty()) return emptyList()

        val photoInfos = mutableListOf<Infrared.CaptureInfo>()
        for (file in files) {
            if (file.isDirectory || !file.name.endsWith(".jpg")) continue

            val captureInfo = Infrared.CaptureInfo(file.name, file.path)
            photoInfos.add(captureInfo)
        }

        return photoInfos
    }

    private fun listVideoCaptures(): List<Infrared.CaptureInfo> {
        val videoDir = AppPath.getVideoDir()

        if (videoDir.isFile || !videoDir.exists()) return emptyList()

        val files = videoDir.listFiles()

        if (files.isNullOrEmpty()) return emptyList()

        val videoInfos = mutableListOf<Infrared.CaptureInfo>()
        for (file in files) {
            if (file.isFile || !file.name.endsWith(".video")) continue

            val captureInfo = Infrared.CaptureInfo(file.name, file.path, Infrared.CAPTURE_VIDEO)
            videoInfos.add(captureInfo)
        }

        return videoInfos
    }

    fun getJpgFile(fileNameWithoutSuffix: String): File? {
        val fullFileName = "$fileNameWithoutSuffix.jpg"
        return File(AppPath.getPhotoDir(), fullFileName)
    }

    fun getRawStreamFile(fileNameWithoutSuffix: String): File? {
        val fullFileName = "$fileNameWithoutSuffix.dat"
        return File(AppPath.getRawDir(), fullFileName)
    }

    fun deleteCapture(fileNameNoSuffix: String): Boolean {
        val rawStreamFile = getRawStreamFile(fileNameNoSuffix)
        val jpgFile = getJpgFile(fileNameNoSuffix)

        if (jpgFile == null) {
            Log.w(Constant.TAG_DEBUG, "Can not delete jpg: $fileNameNoSuffix for it is null.")
            return false
        }
        if (rawStreamFile == null) {
            Log.w(Constant.TAG_DEBUG, "Can not delete jpg: $fileNameNoSuffix for it is null.")
            return false
        }

        if (jpgFile.delete()) {
            Log.w(Constant.TAG_DEBUG, "Success to delete jpg: ${jpgFile.path}.")
        }
        if (rawStreamFile.delete()) {
            Log.w(Constant.TAG_DEBUG, "Success to delete raw: ${jpgFile.path}.")
        }

        return true
    }
}