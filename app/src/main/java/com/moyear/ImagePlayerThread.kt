package com.moyear

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.blankj.utilcode.util.FileIOUtils
import com.moyear.activity.ui.gallery.CapturePreviewFragment
import com.moyear.core.Infrared
import com.moyear.core.Infrared.CaptureInfo
import com.moyear.core.StreamBytes
import com.moyear.global.MyLog
import com.moyear.utils.CustomFileHelper
import com.moyear.utils.ImageUtils
import java.io.File

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/11/28 19:34
 * @Description :
 */
class ImagePlayerThread(
    frameRate: Int
) : Thread() {

    private val TAG = "ImagePlayerThread"

    private var captureInfo: CaptureInfo? = null

    private var isPlaying: Boolean

    private var totalFrames = 0

    private var currentFrame: Int

    private var frameRate = 25

    private var loopPlay = false

    private var isReady = false

    private var surfaceView: SurfaceView? = null

    private var images: List<File>? = null

    private var  operateCallback: OperateCall? = null

    private var jpegDataToDraw: ByteArray ? = null

    interface OperateCall {
        fun onStart()
        fun onPlay(progress: Int, total: Int)
        fun onPause()
        fun onStop()
    }

//    interface InitListener {
//        fun init()
//    }

    init {
        this.frameRate = frameRate
        isPlaying = false
        currentFrame = 0
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
    }

    fun setPlayConfig(captureInfo: CaptureInfo?) {
        this.captureInfo = captureInfo
        currentFrame = 0
        isPlaying = false

        readyForPlay()
    }

    fun getFrameRate(): Int {
        return frameRate
    }

    fun getTotalFrames(): Int {
        return totalFrames
    }
    fun resumePlay() {
        isPlaying = true
    }

    fun pausePlay() {
        isPlaying = false

        operateCallback?.onPause()
    }

    fun setPauseCallback(operateCallback: OperateCall?) {
        this.operateCallback = operateCallback
    }

    private fun check(): Boolean {
        if (captureInfo == null || captureInfo!!.type == Infrared.CAPTURE_PHOTO) {
            Log.d(TAG, "no available video capture")
            return false
        }
        return true
    }

    private var lastTime = System.currentTimeMillis()

    fun drawCaptureInSurface(
        captureInfo: CaptureInfo,
    ) {
        val imgFile = Infrared.findCaptureImageFile(captureInfo)
        if (imgFile != null && !imgFile.exists()) {
            Log.w(CapturePreviewFragment.TAG, "NO Capture Image File Found")
            return
        }

        jpegDataToDraw = FileIOUtils.readFile2BytesByChannel(imgFile)
    }

    override fun run() {
        while (check()) {

            if (jpegDataToDraw != null) {
                drawJpegPicture(jpegDataToDraw)
                jpegDataToDraw = null
            }

            if (!isPlaying) continue

            if (!isReady) {
                if (readyForPlay()) {
                    isReady = true
                    Log.d(TAG, "ready for play: true")
                } else {
                    Log.d(TAG, "ready for play: false")
                }
            }

            if (currentFrame > totalFrames - 1) {
                if (loopPlay) {
                    currentFrame %= totalFrames
                } else {
                    // 播放完成
                    isPlaying = false
                    operateCallback?.onStop()
                    continue
                }
            }

            operateCallback?.onPlay(currentFrame, totalFrames)

            val imageFile = images?.get(currentFrame)

            val bytes = FileIOUtils.readFile2BytesByChannel(imageFile) ?: continue

            val streamBytes = StreamBytes.fromBytes(bytes)

            // 将yuv数据转换成jpg数据，并显示在SurfaceView上
            val dataYUV = streamBytes.getYuvBytes()
            if (dataYUV != null) {
                val yuvImgWidth = BasicConfig.yuvImgWidth
                val yuvImgHeight = BasicConfig.yuvImgHeight
                val jpegData =
                    ImageUtils.yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))
                drawJpegPicture(jpegData)
            }

            var curTime = System.currentTimeMillis()
            Log.d(TAG, "render frame ($currentFrame in $totalFrames), cost : ${(curTime - lastTime)} ms")
            lastTime = curTime

            currentFrame++
            try {
                var sleepTime = (1000 / frameRate).toLong()

                if (sleepTime > 0) {
                    sleep(sleepTime)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun readyForPlay(): Boolean {
        if (!check()) return false

        if (captureInfo == null) return false

        MyLog.d("获取图像序列：${captureInfo!!.name}")

        // 列出raw图像文件序列
        images = CustomFileHelper.listRawFrames(captureInfo!!)

        totalFrames = images?.size ?: 0

        MyLog.d("total frames：${totalFrames}")
        return true
    }

    fun skip(progress: Int) {
        if (progress < 0 || progress > 100) {
            Log.d(TAG, "Please input a valid progress.")
            return
        }

        currentFrame = totalFrames * progress / 100
    }

    private fun drawJpegPicture(jpegData: ByteArray?) {
        if (surfaceView == null) return

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

        val screenWidth = surfaceView?.width ?: -1
        val screenHeight =  surfaceView?.height ?: -1
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
        val canvas = surfaceView!!.holder.lockCanvas() //获取目标画图区域，无参数表示锁定的是全部绘图区
//        canvas.drawColor(Color.BLACK) //清除上次绘制的内容
        val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        canvas?.drawBitmap(bitmap, null, rect, null)
        surfaceView!!.holder.unlockCanvasAndPost(canvas) //解除锁定并显示
    }
}