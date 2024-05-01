package com.example.uptmsmartcampus

import android.view.View
import androidx.viewpager.widget.ViewPager

class CompositePageTransformer : ViewPager.PageTransformer {
    private val depthPageTransformer = DepthPageTransformer()
    private val zoomOutPageTransformer = ZoomOutPageTransformer()

    override fun transformPage(view: View, position: Float) {
        depthPageTransformer.transformPage(view, position)
        zoomOutPageTransformer.transformPage(view, position)
    }
}
