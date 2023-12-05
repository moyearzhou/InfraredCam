package com.moyear.global

import android.util.Log

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/12/5 20:56
 * @Description :
 */
object MyLog {
    fun d(msg: String?) {
        Log.d("ThermalCam", msg!!)
    }
}