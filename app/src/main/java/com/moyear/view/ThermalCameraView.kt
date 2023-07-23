package com.moyear.view

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.hcusbsdk.Interface.FStreamCallBack
import com.hcusbsdk.Interface.JavaInterface
import com.hcusbsdk.Interface.USB_FRAME_INFO
import com.hcusbsdk.Interface.USB_IMAGE_VIDEO_ADJUST
import com.hcusbsdk.Interface.USB_STREAM_CALLBACK_PARAM
import com.hcusbsdk.Interface.USB_THERMAL_STREAM_PARAM
import com.hcusbsdk.Interface.USB_VIDEO_PARAM
import com.moyear.BasicConfig
import com.moyear.IFR_REALTIME_TM_OUTCOME_UPLOAD_INFO
import com.moyear.core.StreamBytes
import com.moyear.core.StreamBytes.Companion.fromBytes
import com.moyear.utils.ImageUtils.Companion.yuvImage2JpegData
import java.util.Arrays

class ThermalCameraView(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {

    private val TAG = "ThermalCameraView"

    private var holder: SurfaceHolder
    private var userId = JavaInterface.USB_INVALID_USER_ID //设备句柄
    private var mDwHandle = JavaInterface.USB_INVALID_CHANNEL //预览句柄

    /**
     * 预览的屏幕分辨率Width
     */
    private var mDwScreenWidth = 256

    /**
     * 预览的屏幕分辨率Height
     */
    private var mDwScreenHeight = 392

    /**
     * 码流分辨率Width
     */
    private var mDwStreamWidth = 256

    /**
     * 码流分辨率Height
     */
    private var mDwStreamHeight = 392

    private var isPreview = false //预览状态： true-正在预览

    private var tempInfo: IFR_REALTIME_TM_OUTCOME_UPLOAD_INFO? = IFR_REALTIME_TM_OUTCOME_UPLOAD_INFO()

    var streamProcessor: StreamProcessor? = null

    var currentFrame = ByteArray(0)
        private set

    enum class CaptureMode {
        MODE_CAPTURE, MODE_RECORD
    }

    var captureMode = CaptureMode.MODE_CAPTURE

//    /**
//     * 背景颜色
//     */
//    var backgroundColor: Int = Color.TRANSPARENT

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}

    //设置设备句柄
    fun setUserID(iUserID: Int) {
        userId = iUserID
        Log.i("[USBDemo]", "SetUserID Success! m_dwUserID:$userId")
    }

    //设置窗口大小
    fun setScreenResolution(width: Int, height: Int) {
        mDwScreenWidth = width
        mDwScreenHeight = height
    }

    //设置码流分辨率
    fun setStreamResolution(width: Int, height: Int) {
        mDwStreamWidth = width
        mDwStreamHeight = height
    }

    //获取预览状态
    fun getPreviewStatus(): Boolean {
        Log.i("[USBDemo]", "GetPreviewStatus Success! m_bPreviewStatus: $isPreview")
        return isPreview
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    //开始预览
    fun startPreview(pHolder: SurfaceHolder): Boolean {
        //获取预览状态
        if (isPreview) {
            //正在预览
            Log.i(TAG, "It is previewing, no need to startPreview()!")
            return true
        }
        holder = pHolder
        if (userId == JavaInterface.USB_INVALID_USER_ID) {
            Log.i(TAG, "Invalid USER ID, unable to startPreview()!!!")
            return false
        }

        //第一步，设置视频参数
        val videoParam = USB_VIDEO_PARAM()
        videoParam.dwVideoFormat = JavaInterface.USB_STREAM_YUY2 // 103
        videoParam.dwWidth = mDwStreamWidth //宽 256
        videoParam.dwHeight = mDwStreamHeight //高 392
        videoParam.dwFramerate = 25 //帧率
        videoParam.dwBitrate = 0 //用不到
        videoParam.dwParamType = 0 //用不到
        videoParam.dwValue = 0 //用不到

        //获取旋转模式
        val videoAdjust = USB_IMAGE_VIDEO_ADJUST()
        JavaInterface.getInstance().USB_GetImageVideoAdjust(userId, videoAdjust)
        if (videoAdjust.byCorridor.toInt() == 1) {
            BasicConfig.yuvImgHeight = 256
            BasicConfig.yuvImgWidth = 192
        } else {
            BasicConfig.yuvImgHeight = 192
            BasicConfig.yuvImgWidth = 256
        }
        if (JavaInterface.getInstance().USB_SetVideoParam(userId, videoParam)) {
            //登录成功
            Log.i(
                "[USBDemo]", "USB_SetVideoParam Success! " +
                        " dwVideoFormat:" + videoParam.dwVideoFormat +
                        " dwWidth:" + videoParam.dwWidth +
                        " dwHeight:" + videoParam.dwHeight +
                        " dwFramerate:" + videoParam.dwFramerate +
                        " dwBitrate:" + videoParam.dwBitrate +
                        " dwParamType:" + videoParam.dwParamType +
                        " dwValue:" + videoParam.dwValue
            )
        } else {
            Log.e(
                "[USBDemo]",
                "USB_SetVideoParam failed! error:" + JavaInterface.getInstance()
                    .USB_GetLastError() +
                        " dwVideoFormat:" + videoParam.dwVideoFormat +
                        " dwWidth:" + videoParam.dwWidth +
                        " dwHeight:" + videoParam.dwHeight +
                        " dwFramerate:" + videoParam.dwFramerate +
                        " dwBitrate:" + videoParam.dwBitrate +
                        " dwParamType:" + videoParam.dwParamType +
                        " dwValue:" + videoParam.dwValue
            )
            return false
        }

        //第二步，开启码流回调
        val callbackParam = USB_STREAM_CALLBACK_PARAM()
        //        struStreamCBParam.dwStreamType = JavaInterface.USB_STREAM_MJPEG; //Mjpeg裸码流
        callbackParam.dwStreamType = JavaInterface.USB_STREAM_YUY2 //
        callbackParam.fnStreamCallBack = fStreamCallBack //回调函数
        mDwHandle = JavaInterface.getInstance().USB_StartStreamCallback(userId, callbackParam)
        if (mDwHandle != JavaInterface.USB_INVALID_CHANNEL) {
            //开启码流回调成功
            Log.i("[USBDemo]", "USB_StartStreamCallback Success! ")
        } else {
            Log.e("[USBDemo]",
                "USB_StartStreamCallback failed! error:" + JavaInterface.getInstance()
                    .USB_GetLastError()
            )
            return false
        }
        setThermalStreamParam(userId)
        isPreview = true
        return true
    }

    //回调函数
    var fStreamCallBack: FStreamCallBack = FnStreamCallBack()

    init {
        holder = getHolder()
        getHolder().addCallback(this)
    }

    inner class FnStreamCallBack : FStreamCallBack {
        override fun invoke(handle: Int, frameInfo: USB_FRAME_INFO) {
            val data = Arrays.copyOf(frameInfo.pBuf, frameInfo.dwBufSize)

            // 更新当前的Frame
            currentFrame = data
            Log.i("test", "invoke: length " + data.size)
            if (!holder.surface.isValid) {
                return
            }
            val streamBytes = fromBytes(data)
            if (streamProcessor != null) {
                streamProcessor!!.onStream(streamBytes)
            }
            tempInfo = streamBytes.getTempInfo()

            // 将yuv数据转换成jpg数据，并显示在SurfaceView上
            val dataYUV = streamBytes.getYuvBytes()
            if (dataYUV != null) {
                val yuvImgWidth = BasicConfig.yuvImgWidth
                val yuvImgHeight = BasicConfig.yuvImgHeight
                val jpegData = yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))
                drawJpegPicture(jpegData)
            }
        }
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
        //        Log.e(TAG, "drawJpegPicture: outWidth=$outWidth outHeight=$outHeight")

        val screenWidth = holder.surfaceFrame.width()
        val screenHeight = holder.surfaceFrame.height()
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
        val canvas = holder.lockCanvas() //获取目标画图区域，无参数表示锁定的是全部绘图区
        canvas.drawColor(Color.BLACK) //清除上次绘制的内容
        val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        canvas?.drawBitmap(bitmap, null, rect, null)
        holder.unlockCanvasAndPost(canvas) //解除锁定并显示
    }

    /**
     * 设置热传感器的参数
     * @param lUserID
     * @return
     */
    private fun setThermalStreamParam(lUserID: Int): Boolean {
        val streamParam = USB_THERMAL_STREAM_PARAM()
        streamParam.byVideoCodingType = BasicConfig.videoCodingType.toByte() //8
        //码流数据编解码类型:
        //1-热成像裸数据; 2-全屏测温数据; 3-实时裸数据; 4-热图数据; 5-热成像实时流; 6-YUV实时数 7-PS封装MJPEG实时流;
        // 8-全屏测温数据+YUV实时流; 9-YUV+裸数据; 10-仅YUV不含测温头; 11-测温头+YUV+裸数据
        return if (JavaInterface.getInstance().USB_SetThermalStreamParam(lUserID, streamParam)) {
            //配置成功
            Log.i(
                "[USBDemo]", "USB_SetThermalStreamParam Success! " +
                        " byVideoCodingType:" + streamParam.byVideoCodingType
            )
            true
        } else {
            Log.e(
                "[USBDemo]",
                "USB_SetThermalStreamParam failed! error:" + JavaInterface.getInstance()
                    .USB_GetLastError() +
                        " byVideoCodingType:" + streamParam.byVideoCodingType
            )
            false
        }
    }

    //停止预览
    fun stopPreview(): Boolean {
        //获取预览状态
        if (!isPreview) {
            //未开启预览
            Log.i("[USBDemo]", "未开启预览!")
            return true
        }
        return if (JavaInterface.getInstance().USB_StopChannel(userId, mDwHandle)) {
            //停止成功
            Log.i(
                "[USBDemo]", "USB_StopChannel Success! " +
                        " m_dwUserID:" + userId +
                        " m_dwHandle:" + mDwHandle
            )
            isPreview = false
            true
        } else {
            Log.e(
                "[USBDemo]",
                "USB_StopChannel failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                        " m_dwUserID:" + userId +
                        " m_dwHandle:" + mDwHandle
            )
            false
        }
    }

    //获取最高温
    val maxTmp: Float
        get() = tempInfo!!.maxTmp

    //获取最低温
    val minTmp: Float
        get() = tempInfo!!.minTmp

    //获取最低温
    val streamType: Int
        get() = tempInfo!!.streamType

    interface StreamProcessor {
        fun onStream(stream: StreamBytes?): Boolean
    }
}