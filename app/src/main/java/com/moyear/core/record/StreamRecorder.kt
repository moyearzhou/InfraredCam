package com.moyear.core.record

import android.util.Log
import com.moyear.core.StreamBytes
import com.moyear.global.MyLog
import com.moyear.global.getCurrentDateTime
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

    private constructor() {}

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

    // page文件写入文件队列
    private val pageFileBuffer = LinkedBlockingQueue<ByteArray>()
    // 缓冲区队列中对应的帧的索引
    private val pageIndexQueue = LinkedBlockingQueue<Int>()

    private val threadFrameProcessor = FrameProcessor()

    private val videoFileWriter = RawVideoWriter()

    private var buffer: ByteArray ?= null

    // 用来记录缓冲区中保存的帧数
    private var frameBufferCount = 0

    private var pagingInterval = 100

    private var frameSize = 201248

    // 最终保存的帧数
    private var frameNumWrite = AtomicInteger(0)

    data class RecordConfig(var name: String,
                            var frameWidth: Int,
                            var frameHeight: Int,
                            var frameRate: Int,
                            ) {
        var pageInterval = 100
        var totalFrames = 0

        var frameByteSze = 201248

        var createTime: String = ""
        var endCaptureTime: String = ""
//        var frameByteSize =
    }

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

        mThreadPool = Executors.newFixedThreadPool(5, MyThreadFactory())

        if (threadFrameProcessor.state == Thread.State.RUNNABLE) {
            MyLog.d("FrameProcessor线程正处于Runnable状态")
        } else {
            // 启动一个线程来处理缓冲区中的帧数据
            threadFrameProcessor.start()
            MyLog.d("启动FrameProcessor线程来处理缓冲区的数据")
        }
    }

    fun popFrame(streamBytes: StreamBytes) {
        if (frameNum.get() == 0) {
            MyLog.d("根据第一帧初始化数据")

            frameSize = streamBytes.getSize()
            buffer = ByteArray(streamBytes.getSize() * pagingInterval)

            val recordConfig = videoFileWriter.getRecordConfig()
            recordConfig?.frameByteSze = frameSize
            recordConfig?.let {
                videoFileWriter.updateVideoConfig(it)
            }

            createThumbnail(streamBytes)
        }

        val curFrameNum = frameNum.get()
        val curFrameIndex = curFrameNum % pagingInterval
        val pageIndex = curFrameNum / pagingInterval

        val startPos = curFrameIndex * frameSize

        MyLog.d("当前是第${curFrameNum}帧, 分页文件为${pageIndex}, 缓冲区的索引位置为：${startPos}")

        if (frameBufferCount < pagingInterval) {
            // 将帧数据加入缓冲区
            System.arraycopy(streamBytes.getRawBytes(), 0, buffer, startPos, streamBytes.getSize())
            frameBufferCount++
        }

        // 缓冲区满了的话把数据加入分页文件写入队列中
        if (frameBufferCount >= pagingInterval) {
            MyLog.d("缓冲区文件${pageIndex}已满，将其提交到写入文件队列中")

            // 记录分页文件索引
            pageIndexQueue.offer(pageIndex)

            // 将缓冲区的分页文件数据加入到写入队列中
            pageFileBuffer.offer(buffer)

            buffer = ByteArray(streamBytes.getSize() * pagingInterval)

            // 重制计数器
            frameBufferCount = 0
        }

        frameNum.getAndIncrement()
    }

    private fun writeFrameToFile(frameIndex: Int, byteArray: ByteArray) {

        videoFileWriter.writeFramePageToFile(frameIndex, byteArray)
    }

    private fun createThumbnail(streamBytes: StreamBytes) {
        videoFileWriter.createThumbnail(streamBytes)
    }

    fun endRecord() {
        // 如果有任务还在进行的话，则先等待任务完成
        try {
            MyLog.d("还有任务没有处理完，阻塞直到全部完成再释放资源")
            // 这种阻塞方式会导致ANR卡死
            future?.get() // 这里会阻塞，直到任务完成
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // todo 更新video的配置信息
        val recordConfig = videoFileWriter.getRecordConfig()
        recordConfig?.endCaptureTime = getCurrentDateTime()
        recordConfig?.totalFrames = frameNumWrite.get()
        recordConfig?.let {
            videoFileWriter.updateVideoConfig(it)
        }

        MyLog.d("录制视频${curVideoFile?.name}结束，释放资源")
        // 结束后释放资源
        videoFileWriter.releaseRes()

        // todo 结束后关闭线程池
        close()

        isVideoFileCreated = false
        isRecording = false
    }

    private fun close() {
        mThreadPool?.shutdown()

        future = null
    }

    inner class FrameProcessor : Thread() {
        override fun run() {
            val flag = true
            while (flag) {
                if (pageFileBuffer.isEmpty()) continue

                try {
                    // 从缓冲区取出帧数据
                    val pageFileData: ByteArray = pageFileBuffer.take()
                    // 获取帧的索引
                    val pageIndex = pageIndexQueue.take()

                    future = mThreadPool?.submit {
                        // 将数据帧写入到文件中
                        writeFrameToFile(pageIndex, pageFileData)


                        // todo 更新配置文件的总帧数
                        frameNumWrite.set((pageIndex * pagingInterval).coerceAtLeast(frameNumWrite.get()))

                        MyLog.d("已经写入${frameNumWrite.get()}帧")

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