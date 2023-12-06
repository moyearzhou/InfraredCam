package com.moyear.utils

import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.moyear.core.Infrared
import java.io.File
import java.nio.ByteBuffer

class Mp4Converter {

    private var frameRate = -1

    private var frameWidth = 720

    private var frameHeight = 1280

    private var bitRate = 2000000

    private var frameInterval = 5

    private var colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface

    fun createVideoFromImages(captureInfo: Infrared.CaptureInfo,
                              outputFilePath: String,
                              ) {

        if (captureInfo.type == Infrared.CAPTURE_PHOTO) return


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



        val imageFiles = File(captureInfo.path).listFiles()

        for (i in imageFiles.indices) {
            val imageFile = imageFiles[i]
            val bitmap = BitmapFactory.decodeFile(imageFile.path)

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
        }

        mediaCodec.stop()
        mediaCodec.release()
        mediaMuxer.stop()
        mediaMuxer.release()
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