package com.moyear.core.record

import android.util.Log
import android.util.Size
import com.blankj.utilcode.util.FileIOUtils
import com.google.gson.GsonBuilder
import com.moyear.BasicConfig
import com.moyear.core.StreamBytes
import com.moyear.global.AppPath
import com.moyear.global.MyLog
import com.moyear.utils.ImageUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    private constructor()

    private var isRecording = false

    private var recordConfig: RecordConfig ?= null

    private var recordDirPath = AppPath.getVideoDir()

    private var isVideoFileCreated = false

    @Deprecated("")
    private var curVideo: File ?= null

    private var frameNum = AtomicInteger(-1)

    private val mThreadPool: ExecutorService = Executors.newFixedThreadPool(1, MyThreadFactory())

    private var curZipFile: File? = null

    private var outputStream: FileOutputStream? = null

    private var zipOutputStream: ZipOutputStream? = null

    private var future: Future<*>? = null

    data class RecordConfig(var name: String,
                            var frameWidth: Int,
                            var frameHeight: Int,
                            var frameRate: Int,
                            var createTime: Long) {
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

        recordConfig = config

        val videoName = config.name

        // todo：创建一个新目录目录, 以及创建视频配置文件
        initVideoFile(videoName)
    }

    private fun initVideoFile(name: String) {
        val videoName = if (name.endsWith(".video")) {
            name
        } else {
            "$name.video"
        }

        // 创建一个新的文件夹：
        curVideo = File(recordDirPath, videoName)
        curVideo?.mkdirs()

        Log.d(TAG, "Create video file dir: ${curVideo?.name}")

        val configFile = File(curVideo, "config.json")
        configFile.createNewFile()
        Log.d(TAG, "Create video config: ${configFile.name}")

        recordConfig?.createTime = System.currentTimeMillis()


        // 写入配置文件

        val gson = GsonBuilder().setPrettyPrinting().create()
        val configJson = gson.toJson(recordConfig)
        FileIOUtils.writeFileFromString(configFile, configJson)

        val rawVideoName = videoName.replace("video", "raws")

//            if (name.endsWith(".raws")) {
//            name
//        } else {
//            "$name.raws"
//        }

        // 实现方式2
        val zipFilePath = recordDirPath.path + File.separator + rawVideoName
        initZipRawRecordFile(zipFilePath)

        if (zipOutputStream != null) {
            // 往压缩文件根目录中写入config.json的配置文件

            val inputStream = configJson.byteInputStream()
            writeToZip(zipOutputStream!!, inputStream, ZipEntry("config.json"))
            inputStream.close()
        }

        isVideoFileCreated = true
        frameNum.set(0)
    }

    fun popFrame(streamBytes: StreamBytes) {
        if (!isVideoFileCreated) {
            Log.d(TAG, "The video file has not been created yet.")
            return
        }
        if (curVideo == null) {
            Log.d(TAG, "The video file is null.")
            return
        }
        if (curVideo!!.isFile) {
            Log.d(TAG, "The video file is not a video directory.")
            return
        }
        if (frameNum.get() < 0) {
            Log.d(TAG, "Error frame num.")
            return
        }

        // todo 把这个放在线程池里面进行
        // 提交任务
        future = mThreadPool.submit {
            Log.d(TAG, "当前frameNum： ${frameNum.get()}   执行在线程 ${Thread.currentThread()}")

            // 生成8位的文件名，不足用0填充
            val frameName = String.format("%0${8}d", frameNum.get())

            if (frameNum.get() == 0) {
                createVideoThumbnail(streamBytes)
            }

            saveFrameFile(frameName, streamBytes)

            frameNum.getAndIncrement()
        }
    }

    private fun saveFrameFile(frameName: String, streamBytes: StreamBytes) {
        val frameFile = File(curVideo, frameName)
        frameFile.createNewFile()

        streamBytes.getRawBytes()?.let {
            FileIOUtils.writeFileFromBytesByChannel(frameFile, it, true)
            Log.d(TAG, "Write new frame: ${frameNum} in ${Thread.currentThread().name}, file size is: ${it.size}")

            if (zipOutputStream != null) {

                // 往压缩文件根目录中写入thumb.jpg的配置文件
                writeToZip(zipOutputStream!!, it.inputStream(), ZipEntry("raw/${frameName}"))
            }
        }
    }


    fun initZipRawRecordFile(zipFilePath: String) {
        curZipFile = File(zipFilePath)

        outputStream = FileOutputStream(curZipFile)
        zipOutputStream = ZipOutputStream(outputStream)
        zipOutputStream?.setLevel(5) // 设置压缩级别，0-9，9为最高压缩率
    }

    private fun zipFolderRecursive(
        sourceFolder: File,
        currentFolder: String,
        zipOutputStream: ZipOutputStream,
        compressedSize: Array<Long>,
        onProgress: (Int, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val fileList = sourceFolder.listFiles() ?: return

        for (file in fileList) {
            if (file.isDirectory) {
                // todo 把文件夹也压缩到zip文件里面
                zipFolderRecursive(file, "$currentFolder/${file.name}", zipOutputStream, compressedSize, onProgress, onError)
            } else {
                val fileInputStream = FileInputStream(file)

                val zipEntry = ZipEntry("$currentFolder/${file.name}")
                zipOutputStream.putNextEntry(zipEntry)

                MyLog.d("Compress file：${zipEntry.name}, progress is: ${compressedSize[0]}")

                val buffer = ByteArray(1024)
                var length: Int
                while (fileInputStream.read(buffer).also { length = it } > 0) {
                    zipOutputStream.write(buffer, 0, length)

                    compressedSize[0] = compressedSize[0] + length
                }
                fileInputStream.close()
            }
        }
    }

    private fun writeToZip(zipOutputStream: ZipOutputStream,
                           inputStream: InputStream,
                           zipEntry: ZipEntry) {

        zipOutputStream.putNextEntry(zipEntry)

        MyLog.d("Append file：${zipEntry.name} to zip file: ${curZipFile?.name}")

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            zipOutputStream.write(buffer, 0, length)
        }
        inputStream.close()
    }


    private fun createVideoThumbnail(streamBytes: StreamBytes) {
        val dataYUV = streamBytes.getYuvBytes()
        if (dataYUV == null) {
            Log.d(TAG, "Can not decode yuv data in StreamBytes")
            return
        }

//        val yuvImgWidth = recordConfig?.frameWidth ?: 0
//        val yuvImgHeight = recordConfig?.frameHeight ?:0

        // todo：修改以下实现方式
        val yuvImgWidth = BasicConfig.yuvImgWidth
        val yuvImgHeight = BasicConfig.yuvImgHeight

        val jpegData = ImageUtils.yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))

        val thumb = File(curVideo, "thumb.jpg")
        thumb.createNewFile()

        FileIOUtils.writeFileFromBytesByChannel(thumb, jpegData, true)
        Log.d(TAG, "Create video thumb image in: ${thumb.path}")

        if (zipOutputStream != null && jpegData != null) {
            // 往压缩文件根目录中写入thumb.jpg的配置文件
            writeToZip(zipOutputStream!!, jpegData.inputStream(), ZipEntry("thumb.jpg"))
        }
    }


    fun endRecord() {
        // todo 把临时序列变成压缩文件进行储存

        curVideo = null

        frameNum.set(-1)
        recordConfig = null

        isVideoFileCreated = false
        isRecording = false


        // 如果有任务还在进行的话，则先等待任务完成
        try {
            future?.get() // 这里会阻塞，直到任务完成
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //  关闭输入流的时候可能还有任务在处理，
        //  如果直接关闭的话会闪退：java.util.ConcurrentModificationException
        // 关闭输出流
        zipOutputStream?.close()
        outputStream?.close()

        curZipFile = null

        // todo 完成后删除创建的临时文件
    }

    fun onDestroy() {
        mThreadPool.shutdown()
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