package com.moyear.core

import com.moyear.BasicConfig
import com.moyear.IFR_REALTIME_TM_OUTCOME_UPLOAD_INFO
import com.moyear.utils.ConvertUtils

class StreamBytes(private val rawBytes: ByteArray?) {

    fun getYuvBytes(): ByteArray? {
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

    fun getTempInfo(): IFR_REALTIME_TM_OUTCOME_UPLOAD_INFO? {
        if (rawBytes == null) return null

        val tempInfo = IFR_REALTIME_TM_OUTCOME_UPLOAD_INFO()
        tempInfo.streamType = BasicConfig.videoCodingType

        if (rawBytes.size > 98304) {
            val envTmpdata = ByteArray(4)
            val minTmpdata = ByteArray(4)
            val maxTmpdata = ByteArray(4)
            val avrTmpdata = ByteArray(4)

            // 从140位开始到152，依次存储env温度（？？？）、最低温度、最高温度、平均温度
            System.arraycopy(rawBytes, 140, envTmpdata, 0, 4)
            System.arraycopy(rawBytes, 144, minTmpdata, 0, 4)
            System.arraycopy(rawBytes, 148, maxTmpdata, 0, 4)
            System.arraycopy(rawBytes, 152, avrTmpdata, 0, 4)

            // 将其转换为float的温度数据
            tempInfo.envTmp = ConvertUtils.bytes2Float(envTmpdata)
            tempInfo.minTmp = ConvertUtils.bytes2Float(minTmpdata)
            tempInfo.maxTmp = ConvertUtils.bytes2Float(maxTmpdata)
            tempInfo.avrTmp = ConvertUtils.bytes2Float(avrTmpdata)
        }
        return tempInfo
    }

    companion object {
        @JvmStatic
        fun fromBytes(rawBytes: ByteArray): StreamBytes {
            val stream = StreamBytes(rawBytes)
            return stream
        }
    }

}