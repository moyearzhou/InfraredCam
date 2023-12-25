package com.moyear.core

import android.util.Size
import com.blankj.utilcode.util.FileIOUtils
import com.moyear.BasicConfig
import com.moyear.global.MyLog
import com.moyear.utils.ImageUtils
import java.io.File

class UncompressedRecordParser : RecordParser {

    private var recordInfo: Infrared.CaptureInfo? = null

    private var recordFile: File? = null

    private var frameCount = 0
    override fun setInfraredRecord(record: Infrared.CaptureInfo?) {
        this.recordInfo = record
        recordFile = File(recordInfo!!.path)

        frameCount = recordFile?.listFiles()
            ?.filter {
                it.extension != "json" && it.name != "thumb.jpg"
            }?.size ?: 0

        MyLog.d("获取图像序列：${record?.name}")
    }

    override fun getFrameCount(): Int {
        return frameCount
    }

    override fun getFrameBytesAt(frameIndex: Int): ByteArray? {
        if (recordInfo == null) return null

        if (frameIndex < 0) {
            MyLog.d("Pleaser input a valid frame index.")
            return null
        }

        // 生成8位的文件名，不足用0填充
        val frameName = String.format("%0${8}d", frameIndex)
        val frameFile = File(recordFile, frameName)

        return FileIOUtils.readFile2BytesByChannel(frameFile)
    }

}