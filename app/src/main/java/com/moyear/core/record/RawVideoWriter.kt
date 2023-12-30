package com.moyear.core.record

import android.util.Log
import android.util.Size
import com.blankj.utilcode.util.FileIOUtils
import com.google.gson.GsonBuilder
import com.moyear.BasicConfig
import com.moyear.core.StreamBytes
import com.moyear.global.AppPath
import com.moyear.global.MyLog
import com.moyear.global.getCurrentDateTime
import com.moyear.utils.ImageUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RawVideoWriter : VideoFileCreator {

    private val TAG = "RawVideoWriter"

    private var recordDirPath = AppPath.getVideoDir()

    private var recordConfig: StreamRecorder.RecordConfig?= null

    private var isVideoFileCreated = false

    private var curVideoFile: File ?= null

    private var rawsDir: File ?= null

//    private var pagingCount = 100

    override fun initVideoFile(name: String, recordConfig: StreamRecorder.RecordConfig): File? {
        this.recordConfig = recordConfig

        val videoName = if (name.endsWith(".video")) {
            name
        } else {
            "$name.video"
        }

        // 创建一个新的文件夹：
        curVideoFile = File(recordDirPath, videoName)
//        curVideoFile?.mkdirs()

        // 创建raw图片的文件夹
        rawsDir = File(curVideoFile, "raw")
        rawsDir?.mkdirs()

        Log.d(TAG, "Create video file dir: ${curVideoFile?.name}")

        recordConfig?.createTime = getCurrentDateTime()

        updateVideoConfig(recordConfig)

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

    fun splitAndCompressBytes(zipOutputStream: ZipOutputStream, input: ByteArray, pageFileIndex: Int, chunkSize: Int) {
        val numChunks = input.size / chunkSize

        var chunk: ByteArray? = ByteArray(chunkSize)

        for (i in 0 until numChunks) {
            System.arraycopy(input, i * chunkSize, chunk, 0, chunkSize)
//            chunk = input.sliceArray(i * chunkSize until (i + 1) * chunkSize)
            val pageIndex = String.format("%08d", i) // 格式化文件名为八位数字，例如：00000000

            val frameIndex = pageFileIndex * 100 + i
            val frameIndexStr = String.format("%08d", frameIndex)

            // 前面的表示
            // 创建 ZipEntry 对象并设置文件名
            val zipEntry = ZipEntry("$pageIndex.$frameIndexStr")
            zipOutputStream.putNextEntry(zipEntry)

            MyLog.d(TAG, "正在写入zipentry: ${zipEntry.name}")

            // 将 chunk 写入 ZipOutputStream
            zipOutputStream.write(chunk)
            zipOutputStream.closeEntry()
        }

        zipOutputStream.close()
        MyLog.d("Split and compress completed.")
    }

    var compressedByteArray: ByteArray? = null

    override fun writeFramePageToFile(framePagingIndex: Int, byteArray: ByteArray) {
        if (rawsDir == null) {
            Log.d(TAG, "The video file is null.")
            return
        }
        if (rawsDir!!.isFile) {
            Log.d(TAG, "The video file is not a video directory.")
            return
        }
        if (framePagingIndex < 0) {
            Log.d(TAG, "Error frame num.")
            return
        }

        // 生成8位的文件名，不足用0填充
        val frameName = String.format("%0${8}d.part", framePagingIndex)

        Log.d(TAG, "正在将第$framePagingIndex 帧写入到文件")

        if (rawsDir == null) {
            MyLog.e("正在录制的视频已经关闭，无法往其中写入新的分页数据")
            return
        }

//        val deflater = Deflater()
//        deflater.setInput(byteArray)
//        deflater.finish()
//
//        val output = ByteArray(byteArray.size)
//        val compressedSize = deflater.deflate(output)
//
//        compressedByteArray = ByteArray(compressedSize)
//        System.arraycopy(output, 0, compressedByteArray, 0, compressedSize)
//
//        MyLog.d("对数据进行压缩，原始大小: ${byteArray.size}，压缩后大小：${compressedSize}")

        // todo 修改是想方式
        val frameFile = File(rawsDir, frameName)

//         创建 ZipOutputStream 对象
        val zipOutputStream = ZipOutputStream(FileOutputStream(frameFile))
        splitAndCompressBytes(zipOutputStream, byteArray, framePagingIndex, 201248)


//        byteArray.let {
////        compressedByteArray?.let {
//            val outputStream = BufferedOutputStream(FileOutputStream(frameFile))
//            outputStream.write(it)
//            outputStream.close()
//
//            FileIOUtils.writeFileFromBytesByChannel(frameFile, it, true)
//            MyLog.d(TAG, "正在写入第 $framePagingIndex 分页文件 in ${Thread.currentThread().name}, file size is: ${it.size}")
//        }
    }

    override fun releaseRes() {
        recordConfig = null
        curVideoFile = null
    }

    override fun updateVideoConfig(recordConfig: StreamRecorder.RecordConfig) {
        // 写入配置文件
        val configFile = File(curVideoFile, "config.json")
        if (!configFile.exists()) {
            configFile.createNewFile()
            Log.d(TAG, "创建视频配置文件: ${configFile.name}")
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        val configJson = gson.toJson(recordConfig)
        FileIOUtils.writeFileFromString(configFile, configJson)

        Log.d(TAG, "创建视频配置${configJson}到文件: ${configFile.name}")
    }

    override fun getRecordConfig(): StreamRecorder.RecordConfig? {
        return recordConfig
    }
}