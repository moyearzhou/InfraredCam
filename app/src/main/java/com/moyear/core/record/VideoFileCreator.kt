package com.moyear.core.record

import com.moyear.core.StreamBytes
import java.io.File

interface VideoFileCreator {

    fun initVideoFile(name: String, recordConfig: StreamRecorder.RecordConfig): File?

    fun createThumbnail(streamBytes: StreamBytes)

    fun writeFramePageToFile(framePagingIndex: Int, byteArray: ByteArray)

    fun releaseRes()

    fun updateVideoConfig(recordConfig: StreamRecorder.RecordConfig)

    fun getRecordConfig(): StreamRecorder.RecordConfig?
}