package com.moyear.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.view.SurfaceHolder
import android.view.SurfaceView

class CaptureSurfaceView(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var surfaceHolder: SurfaceHolder

//    private var imagesList: List<Bitmap> = ArrayList()
    private var currentFrameIndex = 0
    private var frameRate = 30 // 帧率（每秒帧数）
    private var frameDuration: Long = (1000 / frameRate).toLong() // 每帧持续时间（毫秒）
    private val handler = Handler()

    init {
        surfaceHolder = holder
        surfaceHolder.addCallback(this)
        loadImagesFromFolder() // 从文件夹加载图片
    }



    private fun loadImagesFromFolder() {
        // 从文件夹加载所有图片到imagesList
        // 这里假设你已经获取了文件夹中的所有图片并将它们解码为Bitmap对象
        // 将解码后的Bitmap对象添加到imagesList中
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        startRendering() // Surface被创建时开始渲染
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 在Surface尺寸发生变化时调用
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRendering() // Surface被销毁时停止渲染
    }

    private val renderRunnable = object : Runnable {
        override fun run() {



//            drawFrame() // 绘制当前帧


            handler.postDelayed(this, frameDuration) // 按指定帧率延迟执行下一帧的渲染
        }
    }

    private fun drawFrame(bitmap: Bitmap) {
        val canvas: Canvas? = surfaceHolder.lockCanvas()
        if (canvas != null) {
            // 在Canvas上绘制当前帧
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            // 释放Canvas并将绘制的内容提交
            surfaceHolder.unlockCanvasAndPost(canvas)
        }

        // 更新当前帧索引，确保在图片列表范围内循环
//        currentFrameIndex = (currentFrameIndex + 1) % imagesList.size
    }

    private fun startRendering() {
        handler.post(renderRunnable) // 开始渲染
    }

    private fun stopRendering() {
        handler.removeCallbacks(renderRunnable) // 停止渲染
    }

    private fun drawJpegPicture(jpegData: ByteArray?) {
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

        val screenWidth = this.width
        val screenHeight = this.height
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
        val canvas =surfaceHolder.lockCanvas() //获取目标画图区域，无参数表示锁定的是全部绘图区
//        canvas.drawColor(Color.BLACK) //清除上次绘制的内容
        val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        canvas?.drawBitmap(bitmap, null, rect, null)
        surfaceHolder.unlockCanvasAndPost(canvas) //解除锁定并显示
    }

}