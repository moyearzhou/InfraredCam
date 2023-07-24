package com.moyear.activity.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.moyear.Constant
import com.moyear.R
import com.moyear.activity.OnGalleryNavigate
import com.moyear.adapter.GalleryPreviewAdapter
import com.moyear.core.Infrared
import com.moyear.core.StreamBytes
import com.moyear.databinding.FragmentCapturePreviewBinding
import com.moyear.global.GalleryManager
import com.moyear.global.toast
import java.io.File
import java.io.FileInputStream

class CapturePreviewFragment : Fragment(), View.OnClickListener {

    private lateinit var galleryModel: GalleryModel

    private lateinit var viewModel: CapturePreviewViewModel

    private lateinit var mBinding: FragmentCapturePreviewBinding

    var galleryNavigate : OnGalleryNavigate?= null

    private var adapter: GalleryPreviewAdapter ?= null

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

        mBinding.btnBack.setOnClickListener(this)
        mBinding.btnMore.setOnClickListener(this)

        mBinding.btnDelete.setOnClickListener(this)
        mBinding.btnEdit.setOnClickListener(this)
        mBinding.btnInfo.setOnClickListener(this)

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

        mBinding.imgCapture.setOnClickListener {
            viewModel.showToolBar.value = !viewModel.showToolBar.value!!
        }

//        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        adapter = GalleryPreviewAdapter()
//        mBinding.recyclerView.adapter = adapter

        return mBinding.root
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
            mBinding.imgPlay.visibility = View.GONE
        } else if (captureInfo.type == Infrared.CAPTURE_VIDEO) {
            mBinding.imgPlay.visibility = View.VISIBLE
        }

        mBinding.imgPlay.setOnClickListener {
            toast("播放视频，代码待写！！！")
        }

        val imgFile = Infrared.findCaptureImageFile(captureInfo)
        Glide.with(requireContext())
            .load(imgFile)
            .into(mBinding.imgCapture)

//        val index = galleryModel.indexOfCapture(capture.name)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_back -> navigateToGallery()
            R.id.btn_more -> showMoreMenu(p0)
            R.id.btn_delete -> showDeleteConfirmDialog()
            R.id.btn_edit -> toast("代码待写！！！")
            R.id.btn_send -> toast("代码待写！！！")
            R.id.btn_info -> toast("代码待写！！！")
        }
    }

    private fun showDeleteConfirmDialog() {
        val capture = galleryModel.currentPreview.value
        val fileNameNoSuffix = capture?.name?.removeSuffix(".jpg") ?: ""

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("删除")
            .setMessage("是否删除该热成像照片，此操作同时会删除所保存的原始数据，是否确定")
            .setPositiveButton("确定") {
                _,_ ->
                galleryModel.deleteCapture(fileNameNoSuffix)
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