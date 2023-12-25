package com.moyear.utils

import android.util.Size
import com.moyear.BasicConfig
import com.moyear.core.StreamBytes

class RawFrameHelper {
    companion object {

        fun decodeYuvBytesFromRawFrame(rawBytes: ByteArray): ByteArray? {
            if (rawBytes == null) return null

            var dataYUV = ByteArray(98304)
            // 最后的98304位存储的是yuv数据
            if (rawBytes.size > 98304) {
                System.arraycopy(rawBytes, rawBytes.size - 98304, dataYUV, 0, 98304)
            }

            //如果数据位置98304代表当前仅yuv模式，仅yuv
            if (rawBytes.size == 98304) {
                dataYUV = rawBytes
            }
            return dataYUV
        }

        fun decodeJpegBytesFromRawFrame(rawBytes: ByteArray, yuvImgWidth: Int, yuvImgHeight: Int): ByteArray? {
            val yuvBytes = decodeYuvBytesFromRawFrame(rawBytes) ?: return null
            return ImageUtils.yuvImage2JpegData(yuvBytes, Size(yuvImgWidth, yuvImgHeight))

        }

    }
}