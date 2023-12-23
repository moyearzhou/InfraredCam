package com.moyear.activity.ui.gallery

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.moyear.adapter.GalleryListAdapter
import com.moyear.core.Infrared
import com.moyear.callback.OnCapturePreview
import com.moyear.databinding.FragmentGalleryBinding
import com.tencent.bugly.crashreport.CrashReport

class GalleryFragment : Fragment() {

    companion object {
        fun newInstance() = GalleryFragment()
    }

    private lateinit var viewModel: GalleryModel

    private lateinit var mBinding: FragmentGalleryBinding

    private var adapter: GalleryListAdapter ?= null

    var capturePreviewListener: OnCapturePreview?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(GalleryModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentGalleryBinding.inflate(inflater, container, false)

        adapter = GalleryListAdapter()
        adapter?.onClickCallBack = object : GalleryListAdapter.OnClickCallBack{
            override fun onClick(captureInfo: Infrared.CaptureInfo) {
             previewCapture(captureInfo)
            }
        }

        mBinding.recyclerView.layoutManager = StaggeredGridLayoutManager( 3, StaggeredGridLayoutManager.VERTICAL)
        mBinding.recyclerView.adapter = adapter

        viewModel.galleryCaptures.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                showEmptyView()
            } else {
                hideEmptyView()
            }

            adapter?.updateData(it)
        }

        viewModel.updateGallery()

        return mBinding.root
    }

    private fun previewCapture(captureInfo: Infrared.CaptureInfo) {
//        Log.e(Constant.TAG_DEBUG, "Preview capture: ${capture.name}")
        viewModel.currentPreview.value = captureInfo
        capturePreviewListener?.onPreview(captureInfo)
    }

    private fun hideEmptyView() {
        mBinding.emptyView.visibility = View.GONE
    }

    private fun showEmptyView() {
        mBinding.emptyView.visibility = View.VISIBLE
    }



}