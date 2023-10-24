package com.moyear.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.TimeUtils
import com.hcusbsdk.Interface.JavaInterface
import com.hcusbsdk.Interface.USB_DEVICE_INFO
import com.hcusbsdk.Interface.USB_DEVICE_REG_RES
import com.hcusbsdk.Interface.USB_USER_LOGIN_INFO
import com.moyear.Constant
import com.moyear.PreferenceManager
import com.moyear.activity.CameraActivity.Companion.MODE_TAKE_PHOTO
import com.moyear.core.Infrared
import com.moyear.global.AppPath
import com.moyear.global.GalleryManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

class CameraActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)

    var isUsbSdkInit = false //是否初始化
        private set

    private var userId = JavaInterface.USB_INVALID_USER_ID //当前登录的设备句柄
    fun getUserId(): Int {
        return userId
    }

    val curUserId = MutableLiveData(JavaInterface.USB_INVALID_USER_ID) //当前登录的设备句柄

    private var defaultLoginDevice = 0 //默认登录第一个设备

    //设备信息列表
    private var usbDeviceInfos= Array(JavaInterface.MAX_DEVICE_NUM) {USB_DEVICE_INFO()}

    val latestCaptureInfo = MutableLiveData<Infrared.CaptureInfo>()

    val cameraMode = MutableLiveData(MODE_TAKE_PHOTO)

    fun getSdkVersion(): String {
        //获取USB SDK版本
        return String.format("%08x", JavaInterface.getInstance().USB_GetSDKVersion())
    }

    /**
     * 初始化USB SDK
     */
    fun initUsbSdk(): Boolean {
        isUsbSdkInit = JavaInterface.getInstance().USB_Init()
        return isUsbSdkInit
    }

    fun saveSdkLog(): Boolean {
        return JavaInterface.getInstance().USB_SetLogToFile(JavaInterface.INFO_LEVEL, AppPath.DIR_LOG, 1)
    }

    fun getUsbLastError(): Int {
        return JavaInterface.getInstance().USB_GetLastError()
    }

    /**
     * 获取设备个数
     */
    fun getDeviceCount(context: Context): Int {
        return JavaInterface.getInstance().USB_GetDeviceCount(context)
    }

    fun enumDevices(deviceCount: Int, devices: Array<USB_DEVICE_INFO>): Boolean {
        val result = JavaInterface.getInstance().USB_EnumDevices(deviceCount, devices)

        if (result) {
            //打印设备信息
            for (i in 0 until deviceCount) {
                Log.i(Constant.TAG_DEBUG,
                    "USB_EnumDevices Device info is dwIndex:" + devices[i].dwIndex +
                            " dwVID:" + devices[i].dwVID +
                            " dwPID:" + devices[i].dwPID +
                            " szManufacturer:" + devices[i].szManufacturer +
                            " szDeviceName:" + devices[i].szDeviceName +
                            " szSerialNumber:" + devices[i].szSerialNumber +
                            " byHaveAudio:" + devices[i].byHaveAudio
                )
            }
        } else {
            Log.e(Constant.TAG_DEBUG, "USB_EnumDevices failed! error:" + getUsbLastError())
            throw Exception("USB_EnumDevices failed! error:" + getUsbLastError())
        }
        return result
    }

    // 获取设备信息
    @Throws(Exception::class)
    fun enumDevices(context: Context): Array<USB_DEVICE_INFO>? {
        //获取设备个数，第一次调用会申请设备权限，获取FD失败，用户确认权限后重新枚举，才能获取FD
        val deviceCount: Int = getDeviceCount(context)
        if (deviceCount == 0) {
            Log.e(Constant.TAG_DEBUG, "No USB Device found!")
            return null
        }
        if (deviceCount < 0) {
            Log.e(Constant.TAG_DEBUG, "USB_GetDeviceCount failed! error: ${getUsbLastError()}")
            return null
        }
        Log.i(Constant.TAG_DEBUG, "USB_GetDeviceCount Device count is :$deviceCount")

//        var usbDeviceInfos= Array(JavaInterface.MAX_DEVICE_NUM) {USB_DEVICE_INFO()}
        //获取设备信息
        enumDevices(deviceCount, usbDeviceInfos)
        return usbDeviceInfos
    }

    fun loginDeviceWithFd(loginInfo: USB_USER_LOGIN_INFO, deviceRegRes: USB_DEVICE_REG_RES): Boolean {
        //获取设备信息
        userId = JavaInterface.getInstance().USB_Login(loginInfo, deviceRegRes)

        curUserId.value = userId

        if (userId != JavaInterface.USB_INVALID_USER_ID) {
            //登录成功
            Log.i(Constant.TAG_DEBUG, "LoginDeviceWithFd Success! iUserID:" + userId +
                        " dwDevIndex:" + loginInfo.dwDevIndex +
                        " dwVID:" + loginInfo.dwVID +
                        " dwPID:" + loginInfo.dwPID +
                        " dwFd:" + loginInfo.dwFd)
//            isCameraConnected.value = true
            return true
        } else {
//            isCameraConnected.value = false
            Log.e(
                Constant.TAG_DEBUG,
                "LoginDeviceWithFd failed! error:" + getUsbLastError() +
                        " dwDevIndex:" + loginInfo.dwDevIndex +
                        " dwVID:" + loginInfo.dwVID +
                        " dwPID:" + loginInfo.dwPID +
                        " dwFd:" + loginInfo.dwFd
            )
            throw Exception("LoginDeviceWithFd failed! error:" + getUsbLastError())
        }
        return false
    }

    fun loginDevice(context: Context): Boolean {
        val devices = enumDevices(context)
        if (devices != null) {
            val loginInfo = USB_USER_LOGIN_INFO()
            loginInfo.dwTimeout = 5000
            loginInfo.dwDevIndex = devices[defaultLoginDevice].dwIndex
            loginInfo.dwVID = devices[defaultLoginDevice].dwVID
            loginInfo.dwPID = devices[defaultLoginDevice].dwPID
            loginInfo.dwFd = devices[defaultLoginDevice].dwFd
            //loginInfo.byLoginMode = 0;
            //loginInfo.szUserName = "admin"; //如果是门禁设备，需要输入用户名和密码
            //loginInfo.szPassword = "12345"; //如果是门禁设备，需要输入用户名和密码

            val deviceRegRes = USB_DEVICE_REG_RES()

            return loginDeviceWithFd(loginInfo, deviceRegRes)
        }
        return false
    }

    fun cleanUsbSdk(): Boolean {
//        isCameraConnected.value = false
        return JavaInterface.getInstance().USB_Cleanup()
    }

    //注销设备
    fun logoutDevice(): Boolean {
//        isCameraConnected.value = false

        curUserId.value = JavaInterface.USB_INVALID_USER_ID

        return if (JavaInterface.getInstance().USB_Logout(userId)) {
            //登录成功
            Log.i(Constant.TAG_DEBUG, "USB_Logout Success! iUserID: $userId")
            userId = JavaInterface.USB_INVALID_USER_ID
            true
        } else {
            Log.e(Constant.TAG_DEBUG, "USB_Logout failed! error:" + getUsbLastError())
            false
        }
    }

    @Throws(IOException::class)
    fun saveCapture(data: ByteArray, jpegData: ByteArray) {
        val fileNameWithoutSuffix = TimeUtils.date2String(Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + "_" + ((Math.random() * 9 + 1) * 1000).toInt()
        val rawFileName = "$fileNameWithoutSuffix.dat"
        val rawFile = File(AppPath.getRawDir(), rawFileName)

        // stream.dat记录的是传感器传来的原始数据
        val file = FileOutputStream(rawFile)
        file.write(data, 0, data.size)
        file.close()
        val photoFileName = "$fileNameWithoutSuffix.jpg"
        val photoFile = File(AppPath.getPhotoDir(), photoFileName)
        val osImageFile = FileOutputStream(photoFile)
        osImageFile.write(jpegData, 0, jpegData.size)
        osImageFile.close()

        latestCaptureInfo.value = Infrared.CaptureInfo(photoFileName, photoFile.path)

        Log.i("Debug", "Save image file in: " + photoFile.path)
    }

    fun fetchLastCapture() {
        latestCaptureInfo.value = GalleryManager.getInstance().listCaptures().last()
    }



}