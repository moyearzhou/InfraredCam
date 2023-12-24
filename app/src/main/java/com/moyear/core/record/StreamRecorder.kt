package com.moyear.core.record

import android.util.Log
import com.moyear.core.StreamBytes
import com.moyear.global.MyLog
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


class StreamRecorder {

    class MyThreadFactory : ThreadFactory {

        private var count = 1;

        private val threadPrefix = "RecordSavingThread-"
        override fun newThread(p0: Runnable?): Thread {
            val thread = Thread(p0)
            thread.name = threadPrefix + count++
            return thread
        }
    }

    private constructor() {

    }

    @Volatile
    private var isRecording = false

    private var isVideoFileCreated = false

    /**
     * 当前正在写入的视频文件
     */
    private var curVideoFile: File ?= null

    private var frameNum = AtomicInteger(-1)

    private var mThreadPool: ExecutorService? = null

    private var future: Future<*>? = null

    // 缓冲区队列
    private val frameBuffer = LinkedBlockingQueue<ByteArray>()
    // 缓冲区队列中对应的帧的索引
    private val frameIndexQueue = LinkedBlockingQueue<Int>()

    private val threadFrameProcessor = FrameProcessor()

    private val videoFileWriter = RawVideoWriter()

    data class RecordConfig(var name: String,
                            var frameWidth: Int,
                            var frameHeight: Int,
                            var frameRate: Int,
                            var createTime: Long)

//    private fun setRecordConfig(config: RecordConfig) {
//        this.recordConfig = config
//    }

    fun beginRecord(config: RecordConfig?) {
        if (isRecording) {
            Log.w(TAG, "It is recording, can not begin a new record!")
            return
        }
        if (config == null) {
            Log.w(TAG, "No RecordConfig have been assigned, can not begin recording!")
            return
        }

        isRecording = true

        // todo：创建一个新目录目录, 以及创建视频配置文件
        curVideoFile = videoFileWriter.initVideoFile(config.name, config)

        frameNum.set(0)

        isVideoFileCreated = true

        mThreadPool = Executors.newFixedThreadPool(3, MyThreadFactory())

        if (threadFrameProcessor.state == Thread.State.RUNNABLE) {
            MyLog.d("FrameProcessor线程正处于Runnable状态")
        } else {
            // 启动一个线程来处理缓冲区中的帧数据
            threadFrameProcessor.start()
            MyLog.d("启动FrameProcessor线程来处理缓冲区的数据")
        }
    }

    fun popFrame(streamBytes: StreamBytes) {
        // 记录帧索引
        frameIndexQueue.offer(frameNum.get())
        // 将帧加入缓冲区
        frameBuffer.offer(streamBytes.getRawBytes())

        MyLog.d("将第${frameNum.get()}帧数据加入缓冲区")

        if (frameNum.get() == 0) {
            createThumbnail(streamBytes)
        }
        frameNum.getAndIncrement()
    }

    private fun writeFrameToFile(frameIndex: Int, byteArray: ByteArray) {
        videoFileWriter.writeFrameToFile(frameIndex, byteArray)
    }

    private fun createThumbnail(streamBytes: StreamBytes) {
        videoFileWriter.createThumbnail(streamBytes)
    }

    fun endRecord() {
        // todo 把临时序列变成压缩文件进行储存
        isVideoFileCreated = false
        isRecording = false

        // 如果有任务还在进行的话，则先等待任务完成
        try {
            MyLog.d("还有任务没有处理完，阻塞直到全部完成再释放资源")
            future?.get() // 这里会阻塞，直到任务完成
        } catch (e: Exception) {
            e.printStackTrace()
        }
        MyLog.d("录制视频${curVideoFile?.name}结束，释放资源")
        // 结束后释放资源
        videoFileWriter.releaseRes()

        // todo 结束后关闭线程池
        close()
    }

    private fun close() {
        mThreadPool?.shutdown()

        future = null
    }

    inner class FrameProcessor : Thread() {
        override fun run() {
            val flag = true
            while (flag) {
                if (frameBuffer.isEmpty()) continue

                try {
                    // 从缓冲区取出帧数据
                    val frameData: ByteArray = frameBuffer.take()
                    // 获取帧的索引
                    val frameIndex = frameIndexQueue.take()

                    future = mThreadPool?.submit {
                        // 将数据帧写入到文件中
                        writeFrameToFile(frameIndex, frameData)
                    }
                } catch (e: InterruptedException) {
                    MyLog.e("FrameProcessor线程发生了崩溃：$e")
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private val TAG = "StreamRecorder"

        private var INSTANCE: StreamRecorder ?= null

        @JvmStatic
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