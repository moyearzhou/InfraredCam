package com.moyear.core.record

import android.util.Log
import com.moyear.global.AppPath
import java.io.File

class StreamRecorder {

    private constructor()

    private var isRecording = false

    private var recordConfig: RecordConfig ?= null

    private var recordDirPath = AppPath.getVideoDir()

    data class RecordConfig(var name: String,
                            var frameWidth: Int,
                            var frameHeight: Int,
                            var frameRate: Int,
                            var createTime: Long) {
    }

    fun setRecordConfig(config: RecordConfig) {
        this.recordConfig = config
    }

    fun beginRecord() {
        if (isRecording) {
            Log.w(TAG, "It is recording, can not begin a new record!")
            return
        }
        if (recordConfig == null) {
            Log.w(TAG, "No RecordConfig have been assigned, can not begin recording!")
            return
        }

        // todo：创建一个新目录目录, 以及创建视频配置文件
        initVideo("")

    }

    private fun initVideo(name: String) {
        val videoName = if (name.endsWith(".video")) {
            name
        } else {
            "$name.video"
        }

        // 创建一个新的文件夹：
        val curVideo = File(recordDirPath, videoName)
        curVideo.mkdirs()




    }


    fun endRecord() {



        isRecording = false
    }

    fun release() {


    }

    companion object {
        private val TAG = "StreamRecorder"

        private var INSTANCE: StreamRecorder ?= null
        fun getInstance(): StreamRecorder {
            if (INSTANCE == null) {
                synchronized(StreamRecorder.javaClass) {
                    INSTANCE = StreamRecorder()
                }
            }
            return INSTANCE!!
        }
    }

}