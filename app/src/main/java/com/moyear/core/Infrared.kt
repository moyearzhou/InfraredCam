package com.moyear.core

class Infrared {

    companion object {
        const val CAPTURE_PHOTO = 0
        const val CAPTURE_VIDEO = 1
    }

    /**
     * 采集的照片或者视频
     */
    data class CaptureInfo(var name: String, var path: String, var type: Int = CAPTURE_PHOTO) {}

}