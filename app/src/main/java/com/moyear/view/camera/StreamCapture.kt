package com.moyear.view.camera

import com.moyear.core.StreamBytes

class StreamCapture {

    private var isCapturing = false



}

interface onStreamCapture {
    fun onCapture(streamBytes: StreamBytes)
}