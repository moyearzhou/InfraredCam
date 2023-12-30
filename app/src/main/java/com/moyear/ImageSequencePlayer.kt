package com.moyear

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.Log
import android.view.SurfaceView
import com.blankj.utilcode.util.FileIOUtils
import com.moyear.activity.ui.gallery.CapturePreviewFragment
import com.moyear.core.Infrared
import com.moyear.core.Infrared.CaptureInfo
import com.moyear.core.RecordParser
import com.moyear.core.UncompressedRecordParser
import com.moyear.global.MyLog
import com.moyear.utils.RawFrameHelper
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/11/28 19:34
 * @Description :
 */
class ImageSequencePlayer(
    frameRate: Int
) {
    private val TAG = "ImagePlayerThread"

    private val obj = Object()

    private var captureInfo: CaptureInfo? = null

    @Volatile
    private var isPlaying: Boolean

    private var totalFrames = 0

    private var curParsedFrame = AtomicInteger(0)

    private var frameRate = 25

    private var loopPlay = false

    private var surfaceView: SurfaceView? = null

    private var operateCallback: OperateCall? = null

    private var frameBytes: ByteArray? = null

    private var bitmap: Bitmap? = null

    // 缓冲区队列
    private val frameBuffer = LinkedBlockingQueue<ByteArray>()
    // 缓冲区队列中对应的帧的索引
    private val frameIndexQueue = LinkedBlockingQueue<Int>()

    private val imageProcessThread = ImageProcessThread()

    private var imageRenderThread = ImageRenderThread()

    // 最大预先解析数量
    private var MAX_PRE_PARSE_LIMIT = 1000

    private var recordParser: RecordParser = UncompressedRecordParser()

    // 使用该字段来结束线程的运行
    private var disActive = false

    interface OperateCall {
        fun onStart()
        fun onPlay(progress: Int, total: Int)
        fun onPause()
        fun onStop()
    }

    init {
        this.frameRate = frameRate
        isPlaying = false
        curParsedFrame.set(0)
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
    }

    fun setPlayConfig(captureInfo: CaptureInfo?) {
        this.captureInfo = captureInfo

        recordParser.setInfraredRecord(captureInfo)
        totalFrames = recordParser.getFrameCount()

        curParsedFrame.set(0)

        isPlaying = false
    }

    fun getTotalFrames(): Int {
        return totalFrames
    }
    fun resumePlay() {
        isPlaying = true

        if (disActive) {
            active()
        }

    }

    fun pausePlay() {
        isPlaying = false
        operateCallback?.onPause()
    }

    fun startPlay() {
        active()
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

    private var lastParsedTime = System.currentTimeMillis()

    fun drawCaptureInSurface(
        captureInfo: CaptureInfo,
    ) {
        val imgFile = Infrared.findCaptureImageFile(captureInfo)
        if (imgFile != null && !imgFile.exists()) {
            Log.w(CapturePreviewFragment.TAG, "NO Capture Image File Found")
            return
        }

        val jpegData = FileIOUtils.readFile2BytesByChannel(imgFile)
        drawCaptureInSurface(jpegData)
    }

    fun drawCaptureInSurface(
        jpegData: ByteArray?
    ) {
        val isPlayingLast = isPlaying

        drawJpegPicture(jpegData)

        isPlaying = isPlayingLast
    }

    fun seekTo(progress: Int) {
        if (progress < 0 || progress > 100) {
            Log.d(TAG, "Please input a valid progress.")
            return
        }

        curParsedFrame.set(totalFrames * progress / 100)

        synchronized(obj) {
            MyLog.d(TAG, "跳转到第${curParsedFrame.get()}帧，进度：$progress%")

            val isPlayingLast = isPlaying
            isPlaying = false

            // 清空缓冲区的内容
            frameBuffer.clear()
            frameIndexQueue.clear()

            // 暂停播放的时候如果拖动的话会更改显示预览图
            frameBytes = recordParser.getFrameBytesAt(curParsedFrame.get())
            frameBytes?.let {
                val jpegData = RawFrameHelper.
                    decodeJpegBytesFromRawFrame(it, BasicConfig.yuvImgWidth, BasicConfig.yuvImgHeight)
                drawJpegPicture(jpegData)
            }

            isPlaying = isPlayingLast
        }
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
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) //清除上次绘制的内容

        val option = BitmapFactory.Options()
        option.inMutable = true
        option.inPreferredConfig = Bitmap.Config.RGB_565
        bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, option)
        if (bitmap != null) {
            canvas?.drawBitmap(bitmap!!, null, rect, null)
            bitmap?.recycle()
            bitmap = null
        }
        surfaceView!!.holder.unlockCanvasAndPost(canvas) //解除锁定并显示
    }

    inner class ImageProcessThread: Thread() {
        override fun run() {
            while (check()) {
                if (disActive) return

                if (!isPlaying) continue

                if (curParsedFrame.get() > totalFrames - 1) {
                    if (loopPlay) {
                        curParsedFrame.set(curParsedFrame.get()/totalFrames)
                    } else {
                        continue
                    }
                }

                if (frameBuffer.size >= MAX_PRE_PARSE_LIMIT) {
//                    MyLog.d("预先加载的帧数量已经大于上限，等待缓冲区内容消费")
                    continue
                }

                val frameIndexToParse = curParsedFrame.get()
                frameBytes = recordParser.getFrameBytesAt(frameIndexToParse)

                if (frameBytes == null) {
                    MyLog.d("解析第$frameIndexToParse 帧错误, 获取的字节为null")
                }
                frameBytes?.let {
                    val jpegData = RawFrameHelper.
                        decodeJpegBytesFromRawFrame(it, BasicConfig.yuvImgWidth, BasicConfig.yuvImgHeight)

                    frameBuffer.offer(jpegData)
                    frameIndexQueue.offer(frameIndexToParse)

//                    synchronized(obj) {
//                        frameBuffer.offer(jpegData)
//                        frameIndexQueue.offer(frameIndexToParse)
//                    }

                    val curTime = System.currentTimeMillis()
                    MyLog.d(TAG, "解析第[${frameIndexToParse + 1}/$totalFrames]帧到缓冲区, 消耗${(curTime - lastParsedTime)} ms")
                    lastParsedTime = curTime
                }

                curParsedFrame.incrementAndGet()
            }
        }
    }

    private var lastRenderTime = -1L

    inner class ImageRenderThread: Thread() {

        override fun run() {
            while (true) {
                if (disActive) return

                if (frameBuffer.isEmpty()) continue

                if (!isPlaying) continue

                var curRenderFrameIndex = -1
//                var jpegData: ByteArray? = null
                try {
//                    // 从缓冲区取出帧数据
//                    synchronized(obj) {
//                        val jpegData = frameBuffer.take()
//                        // 获取帧的索引
//                        curRenderFrameIndex = frameIndexQueue.take()
//                        val testquee = frameIndexQueue
//
//                        drawJpegPicture(jpegData)
//                    }

                    val jpegData = frameBuffer.poll()

                    frameIndexQueue.poll()?.let {
                        // 获取帧的索引
                        curRenderFrameIndex = it
                    }

                    // 获取帧的索引
//                    curRenderFrameIndex = frameIndexQueue.poll()

                    drawJpegPicture(jpegData)

                } catch (e: InterruptedException) {
                    MyLog.e("ImageRenderThread线程发生了崩溃：$e")
                    e.printStackTrace()
                }

                operateCallback?.onPlay(curRenderFrameIndex, totalFrames)

                if (curRenderFrameIndex >= totalFrames - 1 && !loopPlay) {
                    // 播放完成
                    isPlaying = false
                    operateCallback?.onStop()
                    continue
                }

                try {
                    val sleepTime = (1000 / frameRate).toLong()
                    if (sleepTime > 0) {
                        sleep(sleepTime)
//                        MyLog.d("Thread: ${currentThread().name} sleeping ..... $sleepTime")
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                val curTime = System.currentTimeMillis()
                Log.d(TAG, "渲染第[${curRenderFrameIndex + 1}/$totalFrames]帧图像到SurfaceView, 消耗${(curTime - lastRenderTime)} ms")
                lastRenderTime = curTime
            }
        }
    }

    fun inactive() {
        synchronized(obj) {
            disActive = true
            isPlaying = false

            frameBuffer.clear()
            frameIndexQueue.clear()
        }

        curParsedFrame.set(0)
    }

    fun active() {
        imageProcessThread.name = "ImageProcessThread"
        imageProcessThread.start()

        imageRenderThread.name = "ImageRenderThread"
        imageRenderThread.start()
    }

}
