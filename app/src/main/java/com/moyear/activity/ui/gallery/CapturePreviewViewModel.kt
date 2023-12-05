package com.moyear.activity.ui.gallery

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.moyear.core.Infrared
import com.moyear.global.MyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class CapturePreviewViewModel(application: Application) : AndroidViewModel(application) {

    val showToolBar = MutableLiveData(true)

    val isVideoPlaying = MutableLiveData(false)

    val isVideoLayout = MutableLiveData(false)

    private var currentCompressJob: Job? = null

    fun isVideoPlaying(): Boolean {
        return isVideoPlaying.value == true
    }

    fun performCompressRawVideo(captureInfo: Infrared.CaptureInfo,
                                onProgress: (Int, Int) -> Unit,
                                onDone: () -> Unit,
                                onError: (String) -> Unit) {

        if (captureInfo.type != Infrared.CAPTURE_VIDEO) {
            onError("视频类型错误")
        }

        val captureFile = File(captureInfo.path)

        val outPath = captureFile.parent + File.separator + captureFile.nameWithoutExtension + ".raws"
        Log.d("压缩视频zip", "压缩文件夹：${captureFile.path} to $outPath")

        currentCompressJob = CoroutineScope(Dispatchers.IO).launch {
            zipFolder(captureFile.path, outPath, onProgress, onDone, onError)
        }
    }

    suspend fun cancelCompressJob(captureInfo: Infrared.CaptureInfo) {
        if (currentCompressJob == null) return

        currentCompressJob?.cancelAndJoin()

        //todo 删除产生的临时文件
//        val captureFile = File(captureInfo.path)

//        // 最终的目标文件
//        val outPath = captureFile.parent + File.separator + captureFile.nameWithoutExtension + ".raws"
//        val targetFile = File(outPath)
//        val zipFile = File(targetFile.parent, targetFile.name + ".temp")
//        if (zipFile.exists())
    }

    fun zipFolder(sourceFolderPath: String,
                  zipFilePath: String,
                  onProgress: (Int, Int) -> Unit,
                  onDone: () -> Unit,
                  onError: (String) -> Unit) {
        val sourceFolder = File(sourceFolderPath)

        // 最终的目标文件
        val targetFile = File(zipFilePath)

        // 以temp名为后缀，创建临时文件
        val zipFile = File(targetFile.parent, targetFile.name + ".temp")

        val outputStream = FileOutputStream(zipFile)
        val zipOutputStream = ZipOutputStream(outputStream)
        zipOutputStream.setLevel(6) // 设置压缩级别，0-9，9为最高压缩率

        val totalSize = calculateFolderSize(sourceFolder)

        zipFolderRecursive(sourceFolder, sourceFolder.name, zipOutputStream, totalSize, arrayOf(0L), onProgress, onError)

        zipOutputStream.close()
        outputStream.close()

        // 压缩完成后把文件名改回去
        zipFile.renameTo(targetFile)

        MyLog.d("Success to compress file: ${targetFile.name}")

        // 通知完成
        onDone()
    }

    private fun zipFolderRecursive(
        sourceFolder: File,
        currentFolder: String,
        zipOutputStream: ZipOutputStream,
        totalSize: Long,
        compressedSize: Array<Long>,
        onProgress: (Int, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val fileList = sourceFolder.listFiles() ?: return

        for (file in fileList) {
            if (file.isDirectory) {
                // todo 把文件夹也压缩到zip文件里面
                zipFolderRecursive(file, "$currentFolder/${file.name}", zipOutputStream, totalSize, compressedSize, onProgress, onError)
            } else {
                val fileInputStream = FileInputStream(file)

                val zipEntry = ZipEntry("$currentFolder/${file.name}")
                zipOutputStream.putNextEntry(zipEntry)

                MyLog.d("Compress file：${zipEntry.name}, progress is: ${compressedSize[0]}/$totalSize")

                val buffer = ByteArray(1024)
                var length: Int
                while (fileInputStream.read(buffer).also { length = it } > 0) {
                    zipOutputStream.write(buffer, 0, length)

                    compressedSize[0] = compressedSize[0] + length
                }
                // 在这里更新压缩进度，可以使用回调或发送广播等方式通知UI更新
                onProgress(compressedSize[0].toInt(), totalSize.toInt())

                fileInputStream.close()
            }
        }
    }

    private fun calculateFolderSize(folder: File): Long {
        var size: Long = 0
        val fileList = folder.listFiles() ?: return size

        for (file in fileList) {
            size += if (file.isDirectory) {
                calculateFolderSize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    fun shareCapture(context: Context, capture: Infrared.CaptureInfo) {
        // todo 获取视频video文件，进行分享
        // 获取要分享的文件
        val file: File = File(capture.path)

        // 获取FileProvider的URI
        val fileUri = FileProvider.getUriForFile(context, "com.moyear.thermalcam.fileprovider", file)

        // 创建分享文件的Intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // 启动分享的Activity
        context.startActivity(Intent.createChooser(shareIntent, "文件分享"))
    }

}