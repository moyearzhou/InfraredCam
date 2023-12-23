package com.moyear

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

class MyApplication : Application() {

    val buglyId = "a6f4f7316e"

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, buglyId, false)
    }

}