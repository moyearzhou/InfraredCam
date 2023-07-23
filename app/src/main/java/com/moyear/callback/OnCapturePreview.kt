package com.moyear.callback

import com.moyear.core.Infrared

interface OnCapturePreview {
    fun onPreview(captureInfo: Infrared.CaptureInfo)
}