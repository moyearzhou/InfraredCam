package com.moyear.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moyear.R
import com.moyear.activity.ui.gallery.CapturePreviewFragment
import com.moyear.activity.ui.gallery.GalleryFragment
import com.moyear.core.Infrared
import com.moyear.callback.OnCapturePreview

class GalleryActivity() : AppCompatActivity(), OnGalleryNavigate, OnCapturePreview  {

    private lateinit var galleryFragment: GalleryFragment

    private lateinit var capturePreviewFragment: CapturePreviewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryFragment = GalleryFragment.newInstance()
        capturePreviewFragment = CapturePreviewFragment()
        capturePreviewFragment.galleryNavigate = this

        galleryFragment.capturePreviewListener = this

        if (savedInstanceState == null) {
            navigateToGallery()
        }
    }

    override fun onPreview(captureInfo: Infrared.CaptureInfo) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.container, capturePreviewFragment)
            .commitNow()
    }

    private fun navigateToGallery() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.container, galleryFragment)
            .commitNow()

    }

    override fun onNavigate() {
        navigateToGallery()
    }

}

interface OnGalleryNavigate {
    fun onNavigate()
}
