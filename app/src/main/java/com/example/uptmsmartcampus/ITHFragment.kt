package com.example.uptmsmartcampus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment

class ITHFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ith, container, false)
        webView = view.findViewById(R.id.webView)
        setupWebView()
        // Set title when fragment is created
        updateActivityTitle("Industrial Training Handbook")
        return view
    }

    private fun setupWebView() {
        // Enable JavaScript (if needed)
        webView.settings.javaScriptEnabled = true

        // Load PDF URL
        val pdfUrl = "https://www.uptm.edu.my/images/2023/LI/KUPTM_IND_TRAINING_PRACTICUM_3RD_EDITION.pdf"
        webView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$pdfUrl")
    }

    fun updateActivityTitle(title: String) {
        // Set the title in the parent activity
        if (activity is MainActivity2) {
            (activity as MainActivity2).updateTitle(title)
        }
    }
}