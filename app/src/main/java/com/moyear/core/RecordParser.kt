package com.moyear.core

interface RecordParser {

    fun setInfraredRecord(record: Infrared.CaptureInfo?)

    fun getFrameCount(): Int

    fun getFrameBytesAt(frameIndex: Int): ByteArray?

}