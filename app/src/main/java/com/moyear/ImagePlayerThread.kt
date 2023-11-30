package com.moyear

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import android.util.Size
import android.view.SurfaceView
import com.blankj.utilcode.util.FileIOUtils
import com.moyear.core.Infrared
import com.moyear.core.Infrared.CaptureInfo
import com.moyear.core.StreamBytes
import com.moyear.utils.ImageUtils
import java.io.File

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/11/28 19:34
 * @Description :
 */
class ImagePlayerThread(
    private val surfaceView: SurfaceView,
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

    private var images: List<File>? = null

    private var  operateCallback: OperateCall? = null

    private var initListener: InitListener? = null

    private var isInit = false

    interface OperateCall {
        fun onStart()
        fun onPlay(progress: Int, total: Int)
        fun onPause()
        fun onStop()
    }

    interface InitListener {
        fun init()
    }

    init {
        this.frameRate = frameRate
        isPlaying = false
        currentFrame = 0
    }

    fun setPlayConfig(captureInfo: CaptureInfo?) {
        this.captureInfo = captureInfo
        currentFrame = 0
        isPlaying = false

//        renderFile(captureInfo)
    }

    fun getFrameRate(): Int {
        return frameRate
    }
    fun resumePlay() {
        isPlaying = true
    }

    fun pausePlay() {
        isPlaying = false

        operateCallback?.onPause()
    }

    fun setInitListener(initListener: InitListener) {
        this.initListener = initListener
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

    override fun run() {
        while (check()) {

            if (!isInit) {
                initListener?.init()
                isInit = true
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

            val bytes = FileIOUtils.readFile2BytesByChannel(imageFile)
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

        val videoFile = File(captureInfo!!.path)
        images = videoFile.listFiles()?.filter {
            // 过滤非图像文件
//            if (file.name.equals("config.json") || file.name.equals("thumb.jpg")) continue
            it.extension != "json" && it.name != "thumb.jpg"
        }

        totalFrames = images?.size ?: 0
        return true
    }

    fun renderFile(captureInfo: CaptureInfo?) {
//        if (captureInfo == null) return

        val imgFile = Infrared.findCaptureImageFile(captureInfo!!)
        if (imgFile != null && !imgFile.exists()) {
            Log.w(TAG, "NO findCaptureImageFile")
            return
        }

        val jpgData = FileIOUtils.readFile2BytesByChannel(imgFile)
        drawJpegPicture(jpgData)
    }

    fun skip(progress: Int) {
        if (progress < 0 || progress > 100) {
            Log.d(TAG, "Please input a valid progress.")
            return
        }

        currentFrame = totalFrames * progress / 100
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
        val canvas = surfaceView.holder.lockCanvas() //获取目标画图区域，无参数表示锁定的是全部绘图区
//        canvas.drawColor(Color.BLACK) //清除上次绘制的内容
        val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        canvas?.drawBitmap(bitmap, null, rect, null)
        surfaceView.holder.unlockCanvasAndPost(canvas) //解除锁定并显示
    }
}