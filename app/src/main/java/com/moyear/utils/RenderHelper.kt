package com.moyear.utils

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.view.SurfaceHolder
import android.view.SurfaceView

object RenderHelper {

    fun scaleRenderToFitSurfaceView(
        jpegData: ByteArray,
        surfaceView: SurfaceView
    ) {
        val decodeOptions = BitmapFactory.Options()
        //只获取图像的边界参数, 不解析图像数据
        decodeOptions.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(
            jpegData,
            0,
            jpegData!!.size,
            decodeOptions
        )
        val outWidth = decodeOptions.outWidth.toFloat()
        val outHeight = decodeOptions.outHeight.toFloat()
        //Log.e(TAG, "drawJpegPicture: outWidth=$outWidth outHeight=$outHeight")

        val screenWidth = surfaceView.width
        val screenHeight =  surfaceView.height
        val scaleWith = screenWidth / outWidth
        val scaleHeight = screenHeight / outHeight
        var scale = 0f
        scale = if (scaleWith >= scaleHeight) {
            scaleHeight
        } else {
            scaleWith
        }
        val rect = Rect(
            (screenWidth - outWidth * scale).toInt() / 2,
            (screenHeight - outHeight * scale).toInt() / 2,
            ((screenWidth - outWidth * scale) / 2 + outWidth * scale).toInt(),
            ((screenHeight - outHeight * scale) / 2 + outHeight * scale).toInt()
        )
        val holder = surfaceView.holder

        val canvas = holder.lockCanvas() //获取目标画图区域，无参数表示锁定的是全部绘图区
        val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        canvas?.drawBitmap(bitmap, null, rect, null)
        holder.unlockCanvasAndPost(canvas) //解除锁定并显示
    }

}