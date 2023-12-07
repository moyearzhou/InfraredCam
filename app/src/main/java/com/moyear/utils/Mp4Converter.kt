package com.moyear.utils

import android.R.attr
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Size
import com.blankj.utilcode.util.FileIOUtils
import com.moyear.BasicConfig
import com.moyear.core.Infrared
import com.moyear.core.StreamBytes
import com.moyear.global.MyLog
import java.io.File
import java.nio.ByteBuffer


class Mp4Converter {

    private var frameRate = -1

    private var frameWidth = 720

    private var frameHeight = 1280

    private var bitRate = 2000000

    private var frameInterval = 5

    private var colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface

    private var converterListener: ConvertListener? = null

    private var isEncodingStarted = false

    interface ConvertListener {
        fun onError(errorMsg: String)
        fun onProgress(progress: Int, total: Int)

        fun onDone(file: File)
    }

    fun setConvertListener(convertListener: ConvertListener) {
        this.converterListener = convertListener
    }

    fun createVideoFromImages(captureInfo: Infrared.CaptureInfo,
                              outputFilePath: String) {

        if (captureInfo.type == Infrared.CAPTURE_PHOTO) return

        // 列出raw图像文件序列
        val imageFiles = CustomFileHelper.listRawFrames(captureInfo)

        if (imageFiles == null) {
            MyLog.e("Null raw image in capture: ${captureInfo.name}")
            return
        }


        var videoTrackIndex = -1;

        val outputFile = File(outputFilePath)
        val outputFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, frameWidth, frameHeight)
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval)

        val mediaMuxer = MediaMuxer(outputFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = mediaCodec.createInputSurface()
        mediaCodec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        var presentationTimeUs: Long = 0

        for (i in imageFiles.indices) {
            val imageFile = imageFiles[i]
//            val bitmap = BitmapFactory.decodeFile(imageFile.path)

            val bytes = FileIOUtils.readFile2BytesByChannel(imageFile)
            val streamBytes = StreamBytes.fromBytes(bytes)

            // 将yuv数据转换成jpg数据，并显示在SurfaceView上
            val dataYUV = streamBytes.getYuvBytes()
            if (dataYUV == null) {
                MyLog.e("Null yuv data in file: ${imageFile.path}")
               return
            }

            val yuvImgWidth = BasicConfig.yuvImgWidth
            val yuvImgHeight = BasicConfig.yuvImgHeight
            val jpegData = ImageUtils.yuvImage2JpegData(dataYUV, Size(yuvImgWidth, yuvImgHeight))

            if (jpegData == null) {
                MyLog.e("Null jpeg in file: ${imageFile.path}")
                return
            }

            val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
            if (bitmap == null) {
                MyLog.e("Null bitmap can get from ${imageFile.path}")
                return
            }

            MyLog.d("Drawing frame ${i}/${imageFiles.size}")

            // 将 Bitmap 绘制到 Surface
            val canvas = inputSurface.lockCanvas(null)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            inputSurface.unlockCanvasAndPost(canvas)

            // 获取编码后的视频数据
            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, -1)
            if (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                val outputData = ByteArray(bufferInfo.size)

                if (outputBuffer == null) {
                    continue
                }

                outputBuffer.position(bufferInfo.offset)
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                outputBuffer.get(outputData)

                // 将视频数据写入 MediaMuxer
                val outputByteBuffer = ByteBuffer.wrap(outputData)
                val outputBufferInfo = MediaCodec.BufferInfo()
                outputBufferInfo.offset = 0
                outputBufferInfo.size = bufferInfo.size
                outputBufferInfo.presentationTimeUs = presentationTimeUs
                outputBufferInfo.flags = bufferInfo.flags
                mediaMuxer.writeSampleData(0, outputByteBuffer, outputBufferInfo)

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                presentationTimeUs += 1000000 / frameRate
            }

            bitmap.recycle()

//            if (!isEncodingStarted) {
//                mediaCodec.start()
//                isEncodingStarted = true
//            }
//
//            val inputBuffers = mediaCodec.inputBuffers
//            val outputBuffers = mediaCodec.outputBuffers
//
//            val inputBufferIndex = mediaCodec.dequeueInputBuffer(-1)
//            if (inputBufferIndex >= 0) {
//                val inputBuffer = inputBuffers[inputBufferIndex]
//                inputBuffer.clear()
//                bitmapToByteBuffer(bitmap, inputBuffer)
//                val presentationTimeUs: Long = computePresentationTime(i)
//                mediaCodec.queueInputBuffer(
//                    inputBufferIndex,
//                    0,
//                    inputBuffer.position(),
//                    presentationTimeUs,
//                    0
//                )
//            }
//
//            val bufferInfo = MediaCodec.BufferInfo()
//            var outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
//            while (outputBufferIndex >= 0) {
//                val outputBuffer = outputBuffers[outputBufferIndex]
//                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
//                    bufferInfo.size = 0
//                }
//                if (bufferInfo.size > 0) {
//                    outputBuffer.position(bufferInfo.offset)
//                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
//                    if (videoTrackIndex == -1) {
//                        videoTrackIndex = mediaMuxer.addTrack(mediaCodec.outputFormat)
//                        mediaMuxer.start()
//                    }
//                    mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
//                }
//                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
//                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
//            }
//
//            bitmap.recycle()
        }

        mediaCodec.stop()
        mediaCodec.release()
        mediaMuxer.stop()
        mediaMuxer.release()

        MyLog.d("Success convert raw file to mp4: ${outputFilePath}")
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap, byteBuffer: ByteBuffer) {
        val width = bitmap.width
        val height = bitmap.height
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = argb[index++]
                byteBuffer.put((pixel shr 16 and 0xFF).toByte()) // R
                byteBuffer.put((pixel shr 8 and 0xFF).toByte()) // G
                byteBuffer.put((pixel and 0xFF).toByte()) // B
            }
        }
    }

    private fun computePresentationTime(frameIndex: Int): Long {
        return (132 + frameIndex * 1000000 / frameRate).toLong()
    }


//    class Configer {
//
//        private var frameRate = -1
//
//        private var frameWidth = 720
//
//        private var frameHeight = 1280
//
//    }

}