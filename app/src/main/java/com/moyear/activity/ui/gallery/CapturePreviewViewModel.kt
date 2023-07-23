package com.moyear.activity.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CapturePreviewViewModel(application: Application) : AndroidViewModel(application) {

    val showToolBar = MutableLiveData(true)

}