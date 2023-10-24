package com.moyear.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hcusbsdk.Interface.JavaInterface
import com.moyear.BasicConfig
import com.moyear.Constant
import com.moyear.OnUsbOperateCallback
import com.moyear.R
import com.moyear.UsbReceiver
import com.moyear.core.Infrared.CaptureInfo
import com.moyear.core.Infrared.Companion.findCaptureImageFile
import com.moyear.core.StreamBytes.Companion.fromBytes
import com.moyear.databinding.ActivityCameraBinding
import com.moyear.global.toast
import com.moyear.utils.ImageUtils.Companion.yuvImage2JpegData
import com.moyear.view.ShootView
import com.moyear.view.ShutterTouchEventListener
import com.moyear.view.ThermalCameraView
import com.moyear.viewmodel.CameraActivityViewModel
import java.io.IOException
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var mBinding: ActivityCameraBinding? = null

    //预览类对象
    private var thermalCameraView: ThermalCameraView? = null
    private var mSurfaceView: SurfaceView? = null
    var mHolder: SurfaceHolder? = null

    private var viewModel: CameraActivityViewModel? = null
    private var recordTime = 0L

    private var handler: Handler? = null
    private val usbReceiver = UsbReceiver()
    private var isRecordingTiming = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CameraActivityViewModel::class.java)
        mBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(mBinding!!.root)
        initCameraView()
        initCamModeTab()
        mBinding!!.btnMore.setOnClickListener { view: View -> showMoreMenu(view) }
        mBinding!!.imgGallery.setOnClickListener { view: View? ->
            val intent = Intent(this@CameraActivity, GalleryActivity::class.java)
            startActivity(intent)
        }
        mBinding!!.btnConfig.setOnClickListener { view: View? ->
            toast("代码待写！！")
        }
        mBinding!!.shootView.setShutterTouchListener(object : ShutterTouchEventListener {
            override fun takePicture() {
                takeCapture()
            }

            override fun videoStart() {
                takeRecord()
            }

            override fun videoEnd() {
                endRecording()
            }
        })
        viewModel!!.latestCaptureInfo.observe(this) { captureInfo: CaptureInfo? ->
            if (captureInfo == null) return@observe
            updateLatestCapture(captureInfo)
        }
        viewModel!!.cameraMode.observe(this, Observer { mode: Int ->
            if (mode == MODE_TAKE_PHOTO) {
                switchToPictureMode()
            } else if (mode == MODE_TAKE_VIDEO) {
                switchToVideoMode()
            }
        })
        viewModel!!.curUserId.observe(this) { curUserId: Int ->
            if (curUserId == JavaInterface.USB_INVALID_USER_ID) {
                showEmptyCameraView()
            } else {
                hideEmptyCameraView()
            }
        }
    }

    private fun endRecording() {
        mBinding!!.shootView.setCameraMode(ShootView.OPTION_TAKE_VIDEO)
        thermalCameraView!!.endRecord()
        if (handler != null) {
            handler!!.removeCallbacksAndMessages(null)
        }
        isRecordingTiming = false
        recordTime = 0L
        updateTimeText(recordTime)
    }

    private fun takeRecord() {
        if (viewModel!!.getUserId() == JavaInterface.USB_INVALID_USER_ID) {
            toast("尚未连接到usb相机！")
            return
        }
        mBinding!!.shootView.setCameraMode(ShootView.OPTION_VIDEO_RECORDING)
        thermalCameraView!!.startRecord()
        isRecordingTiming = true
        handler = Handler()
        updateRecordTime()
    }

    private fun updateRecordTime() {
        if (handler == null) return
        handler!!.postDelayed({
            recordTime += 1000
            updateTimeText(recordTime)
            if (isRecordingTiming) {
                updateRecordTime()
            }
        }, 1000)
    }

    private fun updateTimeText(elapsedTime: Long) {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(minutes)
        if (hours > 1) {
            mBinding!!.txtRecordTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            mBinding!!.txtRecordTime.text = String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun switchToVideoMode() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        mBinding!!.layoutRecodeTimer.startAnimation(fadeIn)
        mBinding!!.layoutRecodeTimer.visibility = View.VISIBLE
        mBinding!!.shootView.setCameraMode(ShootView.OPTION_TAKE_VIDEO)
        thermalCameraView!!.captureMode = ThermalCameraView.CaptureMode.MODE_RECORD
    }

    private fun switchToPictureMode() {
        val fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        mBinding!!.layoutRecodeTimer.startAnimation(fadeOut)
        mBinding!!.layoutRecodeTimer.visibility = View.GONE
        mBinding!!.shootView.setCameraMode(ShootView.OPTION_TAKE_PHOTO)
        thermalCameraView!!.captureMode = ThermalCameraView.CaptureMode.MODE_CAPTURE
    }

    private fun initCamModeTab() {
        val tabs = arrayOf(getString(R.string.take_photo), getString(R.string.take_video))
        val tableLayout = mBinding!!.tabMode
        tableLayout.setTabTextColors(Color.WHITE, Color.parseColor("#ffC13132"))
        for (title in tabs) {
            val newTab = tableLayout.newTab()
            newTab.text = title
            tableLayout.addTab(newTab)
        }
        tableLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val text = tab.text
                if (text == getString(R.string.take_photo)) {
                    viewModel!!.cameraMode.setValue(MODE_TAKE_PHOTO)
                } else if (text == getString(R.string.take_video)) {
                    viewModel!!.cameraMode.setValue(MODE_TAKE_VIDEO)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun updateLatestCapture(captureInfo: CaptureInfo) {
        val img = findCaptureImageFile(captureInfo)
        Glide.with(this)
            .load(img)
            .centerCrop()
            .into(mBinding!!.imgGallery)
    }

    private fun showMoreMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_camera_operate, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.show_info -> showSdkInfo()
                R.id.settings -> {
                    navigateToSetting(SettingsActivity.SETTING_ROOT)
                }
                R.id.stream_setting -> {
                    navigateToSetting(SettingsActivity.SETTING_CAMERA)
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun navigateToSetting(settingRoot: String = SettingsActivity.SETTING_ROOT) {
        val intent2 = Intent(this@CameraActivity, SettingsActivity::class.java)
        intent2.putExtra(SettingsActivity.KEY_SETTING_NAME, settingRoot)
        startActivity(intent2)
    }

    private fun showSdkInfo() {
        val builder = AlertDialog.Builder(this)
        val msg = """
            UserId： ${viewModel!!.getUserId()}
            Sdk Version: ${viewModel!!.getSdkVersion()}
            设备数量: ${viewModel!!.getDeviceCount(this)}
            Last Error: ${viewModel!!.getUsbLastError()}
            """.trimIndent()
        builder.setTitle("信息")
            .setMessage(msg)
            .setPositiveButton("确定", null)
            .show()
    }

    private fun takeCapture() {
        if (viewModel!!.getUserId() == JavaInterface.USB_INVALID_USER_ID) {
            toast("尚未连接到usb相机！")
            return
        }
        val data = thermalCameraView!!.currentFrame
        val streamBytes = fromBytes(data)
        var jpegData: ByteArray? = ByteArray(0)
        // 将yuv数据转换成jpg数据，并显示在SurfaceView上
        val dataYUV = streamBytes.getYuvBytes()
        if (dataYUV != null) {
            val yuvImgWidth = BasicConfig.yuvImgWidth
            val yuvImgHeight = BasicConfig.yuvImgHeight
            jpegData = yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))
        }
        try {
            viewModel!!.saveCapture(data, jpegData!!)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        Log.i(TAG, "Action: ========Take a capture!==========")
    }

    private fun initCameraView() {
        thermalCameraView = ThermalCameraView(this)

        //surfaceview
        mSurfaceView = mBinding!!.surfaceView
        mSurfaceView!!.setZOrderOnTop(true)
        mHolder = mSurfaceView!!.holder //得到surfaceView的holder，类似于surfaceView的控制器
        mHolder!!.setFormat(PixelFormat.TRANSLUCENT)

        //把输送给surfaceView的视频画面，直接显示到屏幕上,不要维持它自身的缓冲区
        mHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        mHolder!!.addCallback(this)

        //读写文件权限动态申请  //高版本SDK下AndroidManifest.xml中配置的读写权限不起作用
        checkPermission()
        //初始化SDK
        initUSbSdk()
        usbReceiver.onUsbOperateCallback = object : OnUsbOperateCallback {
            override fun onAttach(usbDevice: UsbDevice) {
                Toast.makeText(this@CameraActivity, "USB设备接入", Toast.LENGTH_SHORT).show()
                checkCameraConnection()
            }

            override fun onDetach(usbDevice: UsbDevice) {
                Toast.makeText(this@CameraActivity, "USB设备拔出", Toast.LENGTH_SHORT).show()
                viewModel!!.logoutDevice()
                showEmptyCameraView()
            }
        }
        checkCameraConnection()

        // 获取上次拍摄的照片并显示在左下角
        viewModel!!.fetchLastCapture()
    }

    /**
     * 检查相机的连接性，如果未连接则连接到usb相机,并显示画面
     */
    @SuppressLint("NewApi")
    private fun checkCameraConnection() {
        //枚举设备信息
        try {
            //登录设备
            connectToCamera()
            // 开始预览并显示画面
            startPreview()
        } catch (e: Exception) {
            Log.e(Constant.TAG_DEBUG, "Error: $e")
            Looper.prepare()
            Toast.makeText(this@CameraActivity, "Error:$e", Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
    }

    /**
     * 连接并登录相机，整个流程按照：USB_Init, USB_GetDeviceCount, USB_EnumDevices, USB_Login的顺序完成
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun connectToCamera(): Boolean {
        // 检查Sdk是否初始化
        if (!viewModel!!.isUsbSdkInit) {
            initUSbSdk()
        }
        return viewModel!!.loginDevice(this)
    }

    private fun initUSbSdk() {
        checkPermission(Manifest.permission.CAMERA)

        //初始化USB SDK
        if (viewModel!!.initUsbSdk()) {
            Log.i(
                Constant.TAG_DEBUG,
                "USB_Init Success! Current SDK Version: " + viewModel!!.getSdkVersion()
            )
        } else {
            Log.e(Constant.TAG_DEBUG, "USB_Init Failed!")
        }

        //开启USBSDK日志，参数说明见使用手册接口说明
        if (viewModel!!.saveSdkLog()) {
            Log.i(Constant.TAG_DEBUG, "USB_SetLogToFile Success!")
        } else {
            Log.e(
                Constant.TAG_DEBUG,
                "USB_SetLogToFile failed! error:" + viewModel!!.getUsbLastError()
            )
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "未获得读写权限")
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    Log.i(TAG, "用户永久拒绝权限申请")
                } else {
                    Log.i(TAG, "申请权限")
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        100
                    )
                }
            }
            Log.i(TAG, "已获得读写权限")
        } else {
            Log.i(TAG, "无需动态申请")
        }
    }

    //指定权限动态申请
    fun checkPermission(sPermission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(
                    this,
                    sPermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "未获得权限")
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, sPermission)) {
                    Log.i(TAG, "用户永久拒绝权限申请")
                } else {
                    Log.i(TAG, "申请权限")
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, arrayOf(sPermission), 100)
                }
            }
            Log.i(TAG, "已获得权限")
        } else {
            Log.i(TAG, "无需动态申请")
        }
    }

    //开始预览
    private fun startPreview(): Boolean {
        val userId = viewModel!!.getUserId()
        if (userId == JavaInterface.USB_INVALID_USER_ID) {
            Log.i(TAG, "Can not to tartPreview()! for UserID is invalid: $userId")
            return false
        }
        thermalCameraView!!.setUserID(userId) //确定预览的设备
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)

        // 设置屏幕分辨率的长宽为屏幕像素的一半
        thermalCameraView!!.setScreenResolution(metric.widthPixels / 2, metric.heightPixels / 2)
        return if (thermalCameraView!!.startPreview(mHolder!!)) {
            //预览成功
            Log.i(
                TAG,
                "StartPreview Success! iUserID:" + viewModel!!.getUserId()
            )
            true
        } else {
            Log.e(
                TAG,
                "StartPreview failed! error:" + viewModel!!.getUsbLastError()
            )

            toast("StartPreview failed! error:" + viewModel!!.getUsbLastError())
            false
        }
    }

    //清理USBSDK资源
    private fun cleanupUsbSdk() {
        if (viewModel!!.cleanUsbSdk()) {
            Log.i(TAG, "USB_Cleanup Success!")
        } else {
            Log.e(TAG, "USB_Cleanup Failed!")
            Toast.makeText(this, "USB_Cleanup Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        thermalCameraView!!.stopPreview()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)
        thermalCameraView!!.startPreview(mHolder!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.logoutDevice()
        cleanupUsbSdk()
        unregisterReceiver(usbReceiver)
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}
    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}
    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}
    private fun showEmptyCameraView() {
        mBinding!!.layoutEmptyCamera.visibility = View.VISIBLE
    }

    private fun hideEmptyCameraView() {
        mBinding!!.layoutEmptyCamera.visibility = View.GONE
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val MODE_TAKE_PHOTO = 111
        const val MODE_TAKE_VIDEO = 222
    }
}