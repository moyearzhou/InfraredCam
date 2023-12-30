package com.moyear.core

import android.graphics.Bitmap

interface RecordParser {

    fun setInfraredRecord(record: Infrared.CaptureInfo?)

    fun getFrameCount(): Int

    fun getFrameBytesAt(frameIndex: Int): ByteArray?

    fun getRecordThumbnail(): Bitmap?

    fun release()

}