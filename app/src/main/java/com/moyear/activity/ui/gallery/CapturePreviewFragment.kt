package com.moyear.activity.ui.gallery

import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.moyear.ImagePlayerThread
import com.moyear.R
import com.moyear.activity.OnGalleryNavigate
import com.moyear.adapter.GalleryPreviewAdapter
import com.moyear.core.Infrared
import com.moyear.core.StreamBytes
import com.moyear.databinding.FragmentCapturePreviewBinding
import com.moyear.global.GalleryManager
import com.moyear.global.toast
import com.sun.jna.StringArray
import kotlinx.android.synthetic.main.layout_camera_not_linked.textView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream


class CapturePreviewFragment : Fragment(), View.OnClickListener {

    private lateinit var galleryModel: GalleryModel

    private lateinit var viewModel: CapturePreviewViewModel

    private lateinit var mBinding: FragmentCapturePreviewBinding

    var galleryNavigate : OnGalleryNavigate?= null

    private var adapter: GalleryPreviewAdapter ?= null

    private lateinit var mSurfaceView: SurfaceView

    private var playerThread: ImagePlayerThread? = null

    private var isThreadStarted = false

    companion object {
        const val TAG = "CapturePreviewFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CapturePreviewViewModel::class.java)

        galleryModel = ViewModelProvider(requireActivity()).get(GalleryModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCapturePreviewBinding.inflate(inflater, container, false)

        mSurfaceView = mBinding.surfaceView

        mBinding.btnBack.setOnClickListener(this)
        mBinding.btnMenu.setOnClickListener(this)

        mBinding.btnDelete.setOnClickListener(this)
        mBinding.btnEdit.setOnClickListener(this)
        mBinding.btnMore.setOnClickListener(this)

        galleryModel.currentPreview.observe(viewLifecycleOwner) {
//            Log.d(Constant.TAG_DEBUG, "Show capture: ${it.name}")
            showCapture(it)
        }

        viewModel.showToolBar.observe(viewLifecycleOwner) {
            if (it) {
                showToolBar()
            } else {
                hideToolBar()
            }
        }

        viewModel.isVideoLayout.observe(viewLifecycleOwner) {
            if (it) {
                initVideoView()
            } else {
                initPhotoView()
            }
        }

        viewModel.isVideoPlaying.observe(viewLifecycleOwner) { isVideoPlaying ->
            if (!isVideoPlaying && viewModel.isVideoLayout.value == true) {
                mBinding.imgPlay.visibility = View.VISIBLE

                mBinding.btnVideoPlay.setImageResource(R.drawable.ic_play_circle)
            } else {
                mBinding.imgPlay.visibility = View.GONE

                mBinding.btnVideoPlay.setImageResource(R.drawable.ic_circle_pause)
            }
        }

        mBinding.surfaceView.setOnClickListener {
            viewModel.showToolBar.value = !viewModel.showToolBar.value!!
        }

        mBinding.btnVideoPlay.setOnClickListener {
            if (viewModel.isVideoPlaying()) {
                pauseVideo()
            } else {
                playVideo()
            }
        }

        return mBinding.root
    }

    private fun initPhotoView() {
        mBinding.imgPlay.visibility = View.GONE
        mBinding.videoControlBar.visibility = View.GONE
    }

    private fun hideToolBar() {
        mBinding.topBar.visibility = View.GONE
        mBinding.bottomBar.visibility = View.GONE
    }

    private fun showToolBar() {
        mBinding.topBar.visibility = View.VISIBLE
        mBinding.bottomBar.visibility = View.VISIBLE
    }

    private fun showCapture(captureInfo: Infrared.CaptureInfo?) {
        if (captureInfo == null) return

        mBinding.txtCaptureTitle.text = captureInfo.name

        if (captureInfo.type == Infrared.CAPTURE_PHOTO) {
            viewModel.isVideoLayout.value = false
        } else if (captureInfo.type == Infrared.CAPTURE_VIDEO) {
            viewModel.isVideoLayout.value = true
        }

        initRenderThread(captureInfo)

        val surfaceHolder = mSurfaceView.holder

        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // 在这里进行渲染操作

                val imgFile = Infrared.findCaptureImageFile(captureInfo!!)
                if (imgFile != null && !imgFile.exists()) {
                    Log.w(TAG, "NO Capture Image File Found")
                    return
                }

                val jpegData = FileIOUtils.readFile2BytesByChannel(imgFile)

                val decodeOptions = BitmapFactory.Options()
                //只获取图像的边界参数, 不解析图像数据
                decodeOptions.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(
                    jpegData,
                    0,
                    jpegData!!.size,
                    decodeOptions
                )
                val outWidth = decodeOptions.outWidth.toFloat()
                val outHeight = decodeOptions.outHeight.toFloat()
                //Log.e(TAG, "drawJpegPicture: outWidth=$outWidth outHeight=$outHeight")

                val screenWidth = mSurfaceView.width
                val screenHeight =  mSurfaceView.height
                val scaleWith = screenWidth / outWidth
                val scaleHeight = screenHeight / outHeight
                var scale = 0f
                scale = if (scaleWith >= scaleHeight) {
                    scaleHeight
                } else {
                    scaleWith
                }
                val rect = Rect(
                    (screenWidth - outWidth * scale).toInt() / 2,
                    (screenHeight - outHeight * scale).toInt() / 2,
                    ((screenWidth - outWidth * scale) / 2 + outWidth * scale).toInt(),
                    ((screenHeight - outHeight * scale) / 2 + outHeight * scale).toInt()
                )
                val canvas = holder.lockCanvas() //获取目标画图区域，无参数表示锁定的是全部绘图区
                val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
                canvas?.drawBitmap(bitmap, null, rect, null)
                holder.unlockCanvasAndPost(canvas) //解除锁定并显示
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                // SurfaceView 大小发生改变时的操作
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // SurfaceView 销毁时的操作
            }
        })


    }

    private fun pauseVideo() {
        if (!viewModel.isVideoPlaying()) {
            return
        }
        // todo 暂定视频播放
        playerThread?.pausePlay()

        viewModel.isVideoPlaying.value = false
    }

    fun convertFramesToTime(fps: Int, frameCount: Int): String {
        val totalSeconds = frameCount / fps
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playVideo() {
        if (viewModel.isVideoPlaying()) {
            return
        }

        if (!isThreadStarted) {
            playerThread?.start()
            isThreadStarted = true
        }

//        playerThread.start()
        playerThread?.resumePlay()

        viewModel.isVideoPlaying.value = true
    }


    private fun initVideoView() {
        mBinding.imgPlay.visibility = View.VISIBLE
        mBinding.videoControlBar.visibility = View.VISIBLE

        mBinding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                playerThread?.skip(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

    }

    private fun initRenderThread(captureInfo: Infrared.CaptureInfo?) {

        mBinding.imgPlay.setOnClickListener {
//            toast("播放视频，代码待写！！！")

            if (captureInfo?.type != Infrared.CAPTURE_VIDEO) {
                Log.d("CapturePreviewFragment", "unsupport opearation!")
                return@setOnClickListener
            }

            if (viewModel.isVideoPlaying()) {
                pauseVideo()
            } else {
                playVideo()
            }
        }

        playerThread = ImagePlayerThread(mSurfaceView, 25)
        playerThread?.setPlayConfig(captureInfo)

        playerThread?.setPauseCallback(object : ImagePlayerThread.OperateCall {
            override fun onStart() {
//                showVideoControlBar()
            }

            override fun onPlay(progress: Int, total: Int) {
                mBinding.progressBar.progress = (100 * progress / total)

                val curTime = convertFramesToTime(25, progress)
                val totalTime = convertFramesToTime(25, total)

                runOnUiThread {
                    mBinding.txtVideoTime.text = "$curTime/$totalTime"
                }

            }
            override fun onPause() {
                mBinding.btnVideoPlay.visibility = View.VISIBLE

            }

            override fun onStop() {
                mBinding.btnVideoPlay.visibility = View.VISIBLE
            }
        })

//        playerThread.start()

    }

    override fun onPause() {
        super.onPause()

        // todo 解决返回后
        playerThread?.pausePlay()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_back -> navigateToGallery()
            R.id.btn_menu -> showMoreMenu(p0)
            R.id.btn_delete -> showDeleteConfirmDialog()
            R.id.btn_edit -> toast("代码待写！！！")
            R.id.btn_send -> toast("代码待写！！！")
            R.id.btn_more -> showMoreOperateMenu()
        }
    }

    private fun showMoreOperateMenu() {
        val listItem = arrayOf("压缩视频成zip", "详情")
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("更多")
            .setItems(listItem) { dialog, position ->
                when (position) {
                    0 -> showCompressDialog()
                    1 -> showCaptureInfo()
                }
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

    private fun showCompressDialog() {
        val capture = galleryModel.currentPreview.value ?: return

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("压缩视频zip")
            .setMessage("正在压缩：${capture.name}")
            .setCancelable(false)
            .setPositiveButton("取消") { p0, p1 ->
                // todo 取消压缩，并删除
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.cancelCompressJob(capture)
                }

                p0?.dismiss()
            }
            .create()
        dialog.show()

        viewModel.performCompressRawVideo(capture, {
            compress, total ->

            val progress = (compress.toDouble() * 100 / total.toDouble()).toInt()
            runOnUiThread {
                dialog.setMessage("正在压缩：${capture.name}，\n进度：$progress%")
            }
        }, {
            runOnUiThread {
                toast("压缩成功")
                dialog.dismiss()
            }
        }, {
            errorMsg ->
            runOnUiThread {
                toast("压缩失败：${errorMsg}")
                dialog.dismiss()
            }

        })
    }

    private fun showDeleteConfirmDialog() {
        val capture = galleryModel.currentPreview.value ?: return

//        val fileNameNoSuffix = capture.name.removeSuffix(".jpg") ?: ""

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("删除")
            .setMessage("是否删除该热成像照片，此操作同时会删除所保存的原始数据，是否确定")
            .setPositiveButton("确定") {
                _,_ ->
                galleryModel.deleteCapture(capture)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun navigateToGallery() {
        galleryNavigate?.onNavigate()
    }

    private fun showMoreMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_capture_operate, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.show_prop -> {
                    showCaptureInfo()
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun showCaptureInfo() {
        val capture = galleryModel.currentPreview.value

        val fileNameNoSuffix = capture?.name?.removeSuffix(".jpg") ?: ""
        val rawFile = GalleryManager.getInstance().getRawStreamFile(fileNameNoSuffix)

        var tempStr = ""
        if (rawFile!!.exists()) {
            val bytes = fileToByteArray(rawFile)
            StreamBytes.fromBytes(bytes).getTempInfo()?.let {
                tempStr =  "平均温度: ${it.avrTmp} ℃\n" +
                        "最高温度: ${it.maxTmp} ℃\n" +
                        "最低温度: ${it.minTmp} ℃\n" +
                        "Env温度: ${it.envTmp} ℃"
            }
        }

        val msg = if (capture == null) {
            "无效热成像图片！！！"
        } else {
            "图片名: \n${capture.name} \n" +
            "路径: \n${capture.path} \n\n" +
            "是否保存原始数据: ${rawFile != null && rawFile.exists()} \n" +
            tempStr
        }


        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("信息")
            .setMessage(msg)
            .setPositiveButton("确定", null)
            .show()
    }

    fun fileToByteArray(file: File): ByteArray {
        val inputStream = FileInputStream(file)
        val byteArray = inputStream.readBytes()
        inputStream.close()
        return byteArray
    }


}