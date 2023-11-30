package com.moyear.activity.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CapturePreviewViewModel(application: Application) : AndroidViewModel(application) {

    val showToolBar = MutableLiveData(true)

    val isVideoPlaying = MutableLiveData(false)

    val isVideoLayout = MutableLiveData(false)

    fun isVideoPlaying(): Boolean {
        return isVideoPlaying.value == true
    }

}