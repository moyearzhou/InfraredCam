package com.moyear.core.record

import android.util.Log
import android.util.Size
import com.blankj.utilcode.util.FileIOUtils
import com.google.gson.GsonBuilder
import com.moyear.BasicConfig
import com.moyear.core.StreamBytes
import com.moyear.global.AppPath
import com.moyear.global.MyLog
import com.moyear.utils.ImageUtils
import java.io.File

class RawVideoWriter : VideoFileCreator {

    private val TAG = "RawVideoWriter"

    private var recordDirPath = AppPath.getVideoDir()

    private var recordConfig: StreamRecorder.RecordConfig?= null

    private var isVideoFileCreated = false

    private var curVideoFile: File ?= null

    override fun initVideoFile(name: String, recordConfig: StreamRecorder.RecordConfig): File? {
        this.recordConfig = recordConfig

        val videoName = if (name.endsWith(".video")) {
            name
        } else {
            "$name.video"
        }

        // 创建一个新的文件夹：
        curVideoFile = File(recordDirPath, videoName)
        curVideoFile?.mkdirs()

        Log.d(TAG, "Create video file dir: ${curVideoFile?.name}")

        val configFile = File(curVideoFile, "config.json")
        configFile.createNewFile()
        Log.d(TAG, "Create video config: ${configFile.name}")

        recordConfig?.createTime = System.currentTimeMillis()

        // 写入配置文件

        val gson = GsonBuilder().setPrettyPrinting().create()
        val configJson = gson.toJson(recordConfig)
        FileIOUtils.writeFileFromString(configFile, configJson)

        isVideoFileCreated = true
        return curVideoFile
    }

    override fun createThumbnail(streamBytes: StreamBytes) {
        val dataYUV = ByteArray(98304)
        streamBytes.readYuvBytes(dataYUV)

        if (dataYUV == null) {
            Log.d(TAG, "Can not decode yuv data in StreamBytes")
            return
        }

        // todo：修改以下实现方式
        val yuvImgWidth = BasicConfig.yuvImgWidth
        val yuvImgHeight = BasicConfig.yuvImgHeight

        val jpegData = ImageUtils.yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))

        val thumb = File(curVideoFile, "thumb.jpg")
        thumb.createNewFile()

        FileIOUtils.writeFileFromBytesByChannel(thumb, jpegData, true)
        MyLog.d(TAG, "Create video thumb image in: ${thumb.path}")
    }

    override fun writeFrameToFile(frameIndex: Int, byteArray: ByteArray) {
        if (curVideoFile == null) {
            Log.d(TAG, "The video file is null.")
            return
        }
        if (curVideoFile!!.isFile) {
            Log.d(TAG, "The video file is not a video directory.")
            return
        }
        if (frameIndex < 0) {
            Log.d(TAG, "Error frame num.")
            return
        }

        // 生成8位的文件名，不足用0填充
        val frameName = String.format("%0${8}d", frameIndex)

        Log.d(TAG, "正在将第$frameIndex 帧写入到文件")

        if (curVideoFile == null) {
            MyLog.e("正在录制的视频已经关闭，无法往其中写入新的帧数据")
            return
        }
        // todo 修改是想方式
        val frameFile = File(curVideoFile, frameName)
        frameFile.createNewFile()

        byteArray.let {
            FileIOUtils.writeFileFromBytesByChannel(frameFile, it, true)
            Log.d(TAG, "Write frame: $frameIndex in ${Thread.currentThread().name}, file size is: ${it.size}")
        }
    }

    override fun releaseRes() {
        recordConfig = null
        curVideoFile = null
    }
}