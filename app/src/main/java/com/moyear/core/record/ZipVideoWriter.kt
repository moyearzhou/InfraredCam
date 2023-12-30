package com.moyear.core.record

import android.util.Log
import android.util.Size
import com.google.gson.GsonBuilder
import com.moyear.BasicConfig
import com.moyear.core.StreamBytes
import com.moyear.global.AppPath
import com.moyear.global.MyLog
import com.moyear.utils.ImageUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipVideoWriter : VideoFileCreator {

    private val TAG = "ZipVideoWriter"

    private var recordConfig: StreamRecorder.RecordConfig?= null

    private var recordDirPath = AppPath.getVideoDir()

    private var outputStream: FileOutputStream? = null

    private var zipOutputStream: ZipOutputStream? = null

    private var isVideoFileCreated = false

    private var curZipFile: File? = null

    private var totalFrame = AtomicInteger(0)

    fun initZipRawRecordFile(zipFilePath: String) {
        curZipFile = File(zipFilePath)

        outputStream = FileOutputStream(curZipFile)
        zipOutputStream = ZipOutputStream(outputStream)
        zipOutputStream?.setLevel(5) // 设置压缩级别，0-9，9为最高压缩率
    }
    override fun initVideoFile(name: String, recordConfig: StreamRecorder.RecordConfig): File? {
        this.recordConfig = recordConfig
        val videoName = if (name.endsWith(".video")) {
            name.replace("video", "raws")
        } else {
            "$name.raws"
        }

        // 实现方式2
        val zipFilePath = recordDirPath.path + File.separator + videoName
        initZipRawRecordFile(zipFilePath)

        if (zipOutputStream != null) {
            // 往压缩文件根目录中写入config.json的配置文件
             val gson = GsonBuilder().setPrettyPrinting().create()
             val configJson = gson.toJson(recordConfig)

            val inputStream = configJson.byteInputStream()
            writeToZip(zipOutputStream!!, inputStream, ZipEntry("config.json"))
            inputStream.close()
        }

        isVideoFileCreated = true

        return curZipFile
    }

    override fun createThumbnail(streamBytes: StreamBytes) {
        val dataYUV = ByteArray(98304)
        streamBytes.readYuvBytes(dataYUV)

//        val dataYUV = streamBytes.getYuvBytes()
        if (dataYUV == null) {
            Log.d(TAG, "Can not decode yuv data in StreamBytes")
            return
        }

        // todo：修改以下实现方式
        val yuvImgWidth = BasicConfig.yuvImgWidth
        val yuvImgHeight = BasicConfig.yuvImgHeight

        val jpegData = ImageUtils.yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))

        if (zipOutputStream != null && jpegData != null) {
            Log.d(TAG, "Create video thumb image in custom video file")
            // 往压缩文件根目录中写入thumb.jpg的配置文件
            writeToZip(zipOutputStream!!, jpegData.inputStream(), ZipEntry("thumb.jpg"))
        }
    }

    override fun writeFramePageToFile(framePagingIndex: Int, byteArray: ByteArray) {
        // 生成8位的文件名，不足用0填充
        val frameName = String.format("%0${8}d", framePagingIndex)

        Log.d(TAG, "正在将第$framePagingIndex 帧写入到文件")

        if (zipOutputStream == null) {
            MyLog.e("正在录制的视频已经关闭，无法往其中写入新的帧数据")
            return
        }

//        totalFrame.set(Math.max(totalFrame.get(), frameIndex))

        byteArray.let {
            Log.d(TAG, "写入第$framePagingIndex 帧，执行在线程： ${Thread.currentThread().name}, file size is: ${it.size}")
            // 往压缩文件根目录中写入thumb.jpg的配置文件
            writeToZip(zipOutputStream!!, it.inputStream(), ZipEntry("raw/${frameName}"))
        }
    }

    override fun releaseRes() {
        //  关闭输入流的时候可能还有任务在处理，
        //  如果直接关闭的话会闪退：java.util.ConcurrentModificationException
        // 关闭输出流
        zipOutputStream?.close()
        outputStream?.close()

        curZipFile = null
    }

    override fun updateVideoConfig(recordConfig: StreamRecorder.RecordConfig) {


    }

    override fun getRecordConfig(): StreamRecorder.RecordConfig? {
        return null
    }

    private fun writeToZip(zipOutputStream: ZipOutputStream,
                           inputStream: InputStream,
                           zipEntry: ZipEntry) {

        zipOutputStream.putNextEntry(zipEntry)

        MyLog.d("Append file：${zipEntry.name} to zip file: ${curZipFile?.name}")

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            zipOutputStream.write(buffer, 0, length)
        }
        inputStream.close()
    }

    private fun zipFolderRecursive(
        sourceFolder: File,
        currentFolder: String,
        zipOutputStream: ZipOutputStream,
        compressedSize: Array<Long>,
        onProgress: (Int, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val fileList = sourceFolder.listFiles() ?: return

        for (file in fileList) {
            if (file.isDirectory) {
                // todo 把文件夹也压缩到zip文件里面
                zipFolderRecursive(file, "$currentFolder/${file.name}", zipOutputStream, compressedSize, onProgress, onError)
            } else {
                val fileInputStream = FileInputStream(file)

                val zipEntry = ZipEntry("$currentFolder/${file.name}")
                zipOutputStream.putNextEntry(zipEntry)

                MyLog.d("Compress file：${zipEntry.name}, progress is: ${compressedSize[0]}")

                val buffer = ByteArray(1024)
                var length: Int
                while (fileInputStream.read(buffer).also { length = it } > 0) {
                    zipOutputStream.write(buffer, 0, length)

                    compressedSize[0] = compressedSize[0] + length
                }
                fileInputStream.close()
            }
        }
    }

}