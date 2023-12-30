package com.moyear.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.FileIOUtils
import com.moyear.global.MyLog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.util.zip.Inflater
import java.util.zip.ZipFile

class UncompressedRecordParser : RecordParser {

    private var recordInfo: Infrared.CaptureInfo? = null

    private var recordFile: File? = null

    private var rawsDir: File? = null

    private var frameCount = 0

    private var pageInterval = 100

    private var frameByteSize = 201248

//    private var randomAccessFile: RandomAccessFile? = null

//    private var pageFile: File ?= null

    private var currentPagePath = ""

//    private var pageFileBytes: ByteArray? = null

    override fun setInfraredRecord(record: Infrared.CaptureInfo?) {
        this.recordInfo = record
        recordFile = File(recordInfo!!.path)

        rawsDir = File(recordFile, "raw")

        frameCount = pageInterval * (rawsDir?.listFiles()
            ?.size ?: 0)

        MyLog.d("获取图像序列：${record?.name}")
    }

    override fun getFrameCount(): Int {
        return frameCount
    }

    private fun readEntryToByteArray(zipFile: ZipFile, indexInPage: Int, frameIndex: Int): ByteArray? {
        val frameIndexStr = String.format("%08d", frameIndex)

//        val startPos = 0
        val startPosStr = String.format("%08d", indexInPage)

        val entryName = "$startPosStr.$frameIndexStr"
//        MyLog.d("正在解析zip entry：${entryName}")

        val entry = zipFile.getEntry(entryName)
        if (entry != null) {
            val inputStream = zipFile.getInputStream(entry)
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()
            return outputStream.toByteArray()
        }
        return null
    }

    private var byteArray: ByteArray? = ByteArray(frameByteSize)

    override fun getFrameBytesAt(frameIndex: Int): ByteArray? {
        if (recordInfo == null) return null

        if (frameIndex < 0) {
            MyLog.d("Pleaser input a valid frame index.")
            return null
        }

        if (rawsDir == null || rawsDir?.exists() == false) {
            MyLog.d("raw目录非法，为null或者不存在")
            return null
        }

        val pageFileIndex = frameIndex / pageInterval
        val index = frameIndex % pageInterval

//        val startPos = index * frameByteSize

        // 生成8位的文件名，不足用0填充
        val pageFileName = String.format("%0${8}d.part", pageFileIndex)
        val pageFile = File(rawsDir, pageFileName)

        MyLog.d("正在解析第${frameIndex}帧,从分页文件${pageFile.path}中获取第${index}个")

        var zipFile: ZipFile ?= null
        try {
            zipFile = ZipFile(pageFile.path)
            byteArray = readEntryToByteArray(zipFile, index, frameIndex)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            zipFile?.close()
        }
//        MyLog.d("解析的数组长度为：${byteArray?.size ?: 0}字节")
        return byteArray
    }

    override fun getRecordThumbnail(): Bitmap? {
        if (recordFile == null) return null

        val imgFile = File(recordFile, "thumb.jpg")
        if (!imgFile.exists()) return null

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888 // 设置Bitmap的颜色配置
        return BitmapFactory.decodeFile(imgFile.path, options)
    }

    override fun release() {

    }

}