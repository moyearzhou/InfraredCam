package com.moyear.core.record

import android.util.Log
import android.util.Size
import com.blankj.utilcode.util.FileIOUtils
import com.google.gson.GsonBuilder
import com.moyear.BasicConfig
import com.moyear.core.StreamBytes
import com.moyear.global.AppPath
import com.moyear.utils.ImageUtils
import java.io.File

class StreamRecorder {

    private constructor()

    private var isRecording = false

    private var recordConfig: RecordConfig ?= null

    private var recordDirPath = AppPath.getVideoDir()

    private var isVideoFileCreated = false

    private var curVideo: File ?= null

    private var frameNum = -1

    data class RecordConfig(var name: String,
                            var frameWidth: Int,
                            var frameHeight: Int,
                            var frameRate: Int,
                            var createTime: Long) {
    }

//    private fun setRecordConfig(config: RecordConfig) {
//        this.recordConfig = config
//    }

    fun beginRecord(config: RecordConfig?) {
        if (isRecording) {
            Log.w(TAG, "It is recording, can not begin a new record!")
            return
        }
        if (config == null) {
            Log.w(TAG, "No RecordConfig have been assigned, can not begin recording!")
            return
        }

        recordConfig = config

        val videoName = config.name

        // todo：创建一个新目录目录, 以及创建视频配置文件
        initVideoFile(videoName)

    }

    private fun initVideoFile(name: String) {
        val videoName = if (name.endsWith(".video")) {
            name
        } else {
            "$name.video"
        }

        // 创建一个新的文件夹：
        curVideo = File(recordDirPath, videoName)
        curVideo?.mkdirs()

        Log.d(TAG, "Create video file dir: ${curVideo?.name}")

        val configFile = File(curVideo, "config.json")
        configFile.createNewFile()
        Log.d(TAG, "Create video config: ${configFile.name}")

        recordConfig?.createTime = System.currentTimeMillis()

        val gson = GsonBuilder().setPrettyPrinting().create()
        val configJson = gson.toJson(recordConfig)
        FileIOUtils.writeFileFromString(configFile, configJson)

        isVideoFileCreated = true
        frameNum = 0
    }

    fun popFrame(streamBytes: StreamBytes) {
        if (!isVideoFileCreated) {
            Log.d(TAG, "The video file has not been created yet.")
            return
        }

        if (curVideo == null) {
            Log.d(TAG, "The video file is null.")
            return
        }

        if (curVideo!!.isFile) {
            Log.d(TAG, "The video file is not a video directory.")
            return
        }

        if (frameNum < 0) {
            Log.d(TAG, "Error frame num.")
            return
        }

        // 生成8位的文件名，不足用0填充
        val frameName = String.format("%0${8}d", frameNum)
        val frameFile = File(curVideo, frameName)
        frameFile.createNewFile()

        if (frameNum == 0) {
            createVideoThumbnail(streamBytes)
        }

        streamBytes.getRawBytes()?.let {


            FileIOUtils.writeFileFromBytesByChannel(frameFile, it, true)
            Log.d(TAG, "Write new frame: ${frameNum}, file size is: ${it.size}")
        }

        frameNum += 1
    }

    private fun createVideoThumbnail(streamBytes: StreamBytes) {
        val dataYUV = streamBytes.getYuvBytes()
        if (dataYUV == null) {
            Log.d(TAG, "Can not decode yuv data in StreamBytes")
            return
        }

//        val yuvImgWidth = recordConfig?.frameWidth ?: 0
//        val yuvImgHeight = recordConfig?.frameHeight ?:0

        // todo：修改以下实现方式
        val yuvImgWidth = BasicConfig.yuvImgWidth
        val yuvImgHeight = BasicConfig.yuvImgHeight

        val jpegData = ImageUtils.yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))

        val thumb = File(curVideo, "thumb.jpg")
        thumb.createNewFile()

        FileIOUtils.writeFileFromBytesByChannel(thumb, jpegData, true)
        Log.d(TAG, "Create video thumb image in: ${thumb.path}")
    }


    fun endRecord() {
        curVideo = null

        frameNum = -1
        recordConfig = null

        isVideoFileCreated = false
        isRecording = false
    }

    companion object {
        private val TAG = "StreamRecorder"

        private var INSTANCE: StreamRecorder ?= null

        @JvmStatic
        fun getInstance(): StreamRecorder {
            if (INSTANCE == null) {
                synchronized(StreamRecorder.javaClass) {
                    INSTANCE = StreamRecorder()
                }
            }
            return INSTANCE!!
        }
    }

}