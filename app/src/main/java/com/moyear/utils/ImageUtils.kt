package com.moyear.utils

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageUtils {

    companion object {

        /**
         * 将yuv的图像数据准换成jpg的图像数据
         * todo 节省内存
         */
        @JvmStatic
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun yuvImage2JpegData(
                yuvImageData: ByteArray,
                yuvImgSize: Size
        ): ByteArray? {
            val yuvImage = YuvImage(
                    yuvImageData,
                    ImageFormat.YUY2,
                    yuvImgSize.width,
                    yuvImgSize.height,
                    null
            )
            val byteArrayOutputStream = ByteArrayOutputStream()
            val rect = Rect(0, 0, yuvImgSize.width, yuvImgSize.height)
            if (yuvImage.compressToJpeg(rect, 100, byteArrayOutputStream)) {
                try {
                    byteArrayOutputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return byteArrayOutputStream.toByteArray()
        }

    }



}