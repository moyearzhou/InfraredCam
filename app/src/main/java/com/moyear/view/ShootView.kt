package com.moyear.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.moyear.Constant

interface ShutterTouchEventListener {
    fun takePicture()
    fun videoStart()
    fun videoEnd()
}

class ShootView : View {

    // 定义当前的操作
    companion object {
        const val OPTION_UNKNOWN = 0
        const val OPTION_TAKE_PHOTO = 1
        const val OPTION_TAKE_VIDEO = 2
        const val OPTION_VIDEO_RECORDING = 3
    }

    private var option = OPTION_TAKE_PHOTO

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mColor = Color.WHITE

    var listener: ShutterTouchEventListener? = null

    // 圆心x坐标
    var centerX = 0f

    // 圆心y坐标
    var centerY = 0f

    // 初始半径
    var radius = 0f

    // 绘制的半径
    var drawRadius = 0f

    // 缩小的半径的最小值
    var minRadius = 0f

    // 缩小的半径的最大值
    var maxRadius = 0f

    // 画笔的不透明度
    var paintAlpha = 255

    // 开始按下去的动画
    lateinit var pictureAnimator: ValueAnimator
    private var currentPictureValue = 0f
    private var pictureDuration = 300L

    // 长按执行到Video录制的动画
    private lateinit var videoAnimator: ValueAnimator
    private var currentVideoValue = 0f
    private var videoDuration = 300L

    // 拍照或者录像动画结束时的半径
    private var animEndRadius = 0f

    private var recordBackgroundColor = Color.parseColor("#ffC13132")

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = mColor
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initPictureAnim()
        initVideoAnim()
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        rotation = -90f
    }

    fun getCameraMode(): Int {
        return option
    }

    fun setCameraMode(mode: Int) {
        when (mode) {
            OPTION_TAKE_VIDEO,
            OPTION_TAKE_PHOTO,
            OPTION_VIDEO_RECORDING -> {
                option = mode
                invalidate()
            }
            else -> Log.w(Constant.TAG_DEBUG, "Unsupported camera mode: $mode !!")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (option) {
            OPTION_UNKNOWN -> drawUnknownOp(canvas)
            OPTION_TAKE_PHOTO -> drawTakePicture(canvas)
            OPTION_TAKE_VIDEO -> drawTakeVideo(canvas)
            OPTION_VIDEO_RECORDING -> drawVideoRecording(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
        radius = if (centerX < centerY) {
            centerX / 10 * 8
        } else {
            centerY / 10 * 8
        }
        drawRadius = radius
        minRadius = centerX / 10 * 7
        maxRadius = centerX / 10 * 8
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> actionDown()
            MotionEvent.ACTION_UP -> actionUp()
        }
        return super.onTouchEvent(event)
    }

    private fun actionDown() {
        if (option  == OPTION_TAKE_PHOTO) {
            pictureAnimator.start()
        } else if (option  == OPTION_TAKE_VIDEO || option  ==  OPTION_VIDEO_RECORDING) {
            videoAnimator.start()
        }
    }

    private fun actionUp() {
        if (option == OPTION_TAKE_PHOTO) {
            pictureAnimator.cancel()
        } else {
            videoAnimator.cancel()
        }
    }

    private fun initPictureAnim() {
        pictureAnimator = ValueAnimator.ofFloat(0F, 100F)
        pictureAnimator.duration = pictureDuration
        pictureAnimator.addUpdateListener { valueAnimator ->
            currentPictureValue = valueAnimator.animatedValue as Float
            if (currentPictureValue < 100F / 4) {
                drawRadius = this.radius - (this.radius - minRadius) * (currentPictureValue / (100f / 4))
                paintAlpha = 255
            } else if (currentPictureValue > 100F / 4 && currentPictureValue < (100F) / 4 * 3) {
                drawRadius = minRadius
                paintAlpha = 255
            } else {
                drawRadius = minRadius + (maxRadius - minRadius) * ((currentPictureValue - (100f / 4 * 3)) / (100f / 4))
                paintAlpha = (255 - 205 * (currentPictureValue - 100f / 4 * 3) / (100f / 4)).toInt()
            }
            postInvalidate()
        }
        pictureAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                option = OPTION_TAKE_PHOTO
                listener?.takePicture()
            }

            override fun onAnimationEnd(p0: Animator?) {}

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationRepeat(p0: Animator?) {}
        })
    }

    private fun initVideoAnim() {
        videoAnimator = ValueAnimator.ofFloat(0F, 100F)
        videoAnimator.duration = videoDuration
        videoAnimator.addUpdateListener { valueAnimator ->
            currentVideoValue = valueAnimator.animatedValue as Float

            if (currentVideoValue < 100F / 4) {
                drawRadius = this.radius - (this.radius - minRadius) * (currentVideoValue / (100f / 4))
                paintAlpha = 255
            } else if (currentVideoValue > 100F / 4 && currentVideoValue < (100F) / 4 * 3) {
                drawRadius = minRadius
                paintAlpha = 255
            } else {
                drawRadius = minRadius + (maxRadius - minRadius) * ((currentVideoValue - (100f / 4 * 3)) / (100f / 4))
                paintAlpha = (255 - 205 * (currentVideoValue - 100f / 4 * 3) / (100f / 4)).toInt()
            }

            postInvalidate()
        }
        videoAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                if (option == OPTION_VIDEO_RECORDING) {
                    listener?.videoEnd()
                } else {
                    listener?.videoStart()
                }
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (option == OPTION_TAKE_VIDEO) {
                    option = OPTION_VIDEO_RECORDING
                } else if (option == OPTION_VIDEO_RECORDING) {
                    option = OPTION_TAKE_VIDEO
                }

                animEndRadius = drawRadius
            }

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationRepeat(p0: Animator?) {}
        })
    }

    private fun drawUnknownOp(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.alpha = 255
        canvas.drawCircle(centerX, centerY, drawRadius, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, (drawRadius * 0.8).toFloat(), paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawTakePicture(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.alpha = 255

        paint.style = Paint.Style.STROKE
        canvas.drawCircle(centerX, centerY, drawRadius, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, (drawRadius * 0.8).toFloat(), paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawTakeVideo(canvas: Canvas) {
        // 绘制录制按钮的背景
        paint.color = recordBackgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, (drawRadius * 0.8).toFloat(), paint)
        paint.style = Paint.Style.STROKE

        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, drawRadius, paint)
    }

    private fun drawVideoRecording(canvas: Canvas) {
        paint.color = recordBackgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, (drawRadius * 0.8).toFloat(), paint)

        paint.color = Color.WHITE
        val width = drawRadius * 0.5
        val rect = RectF((centerX - width/2).toFloat(), (centerY + width/2).toFloat(), (centerX + width/2).toFloat(), (centerY - width/2).toFloat())
        canvas.drawRoundRect(rect, 10f, 10f, paint)

        // 绘制中间的暂停按钮
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(centerX, centerY, drawRadius, paint)

        paint.color = Color.WHITE
    }

    fun setShutterTouchListener(listener: ShutterTouchEventListener) {
        this.listener = listener
    }
}