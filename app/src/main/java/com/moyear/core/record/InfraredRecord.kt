package com.moyear.core.record

import com.moyear.core.Infrared
import java.io.File

class InfraredRecord {

    private var name = ""

    private var path = ""

    private constructor(name: String, path: String) {
        this.name = name
        this.path = path

        decodeRecordInfo()
    }

    private fun decodeRecordInfo() {
        // todo 实现该方法
    }

//    private fun listRawFramesFiles(): Array<File>{
//        val filePath = File(path)
//
//
//    }

    companion object {

        fun from(captureInfo: Infrared.CaptureInfo): InfraredRecord? {
            if (captureInfo.type == Infrared.CAPTURE_PHOTO) return null

            return InfraredRecord(captureInfo.name, captureInfo.path)
        }

    }


}