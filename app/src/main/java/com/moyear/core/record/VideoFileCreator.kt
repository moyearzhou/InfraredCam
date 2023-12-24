package com.moyear.core.record

import com.moyear.core.StreamBytes
import java.io.File

interface VideoFileCreator {

    fun initVideoFile(name: String, recordConfig: StreamRecorder.RecordConfig): File?

    fun createThumbnail(streamBytes: StreamBytes)

    fun writeFrameToFile(frameIndex: Int, byteArray: ByteArray)

    fun releaseRes()
}