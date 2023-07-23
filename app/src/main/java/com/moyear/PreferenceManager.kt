package com.moyear

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isCameraInstantLog(): Boolean {
        return sharedPreferences.getBoolean("camera_instant_log", false)
    }

}