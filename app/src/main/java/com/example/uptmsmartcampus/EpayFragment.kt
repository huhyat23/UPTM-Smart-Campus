package com.example.uptmsmartcampus

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File

class EpayFragment : Fragment() {

    private var webUrl: String = "https://epay.kptm.edu.my/login.php?branch=kl"
    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf()
    } else {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private var isLoaded = false

    private lateinit var loadingDialog: Dialog
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_epay, container, false)
        webView = view.findViewById(R.id.webView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = Dialog(requireContext())

        loadingDialog.window!!.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        loadingDialog.setCancelable(false)
        loadingDialog.show()

        val setting = webView.settings
        setting.javaScriptEnabled = true
        setting.allowFileAccess = true
        setting.domStorageEnabled = true
        setting.javaScriptCanOpenWindowsAutomatically = true
        setting.supportMultipleWindows()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                setProgressDialogVisibility(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val url = request?.url.toString()
                view?.loadUrl(url)
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoaded = true
                webUrl = url ?: ""
                setProgressDialogVisibility(false)
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                isLoaded = false
                setProgressDialogVisibility(false)
                super.onReceivedError(view, request, error)
            }

        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            if (checkMultiplePermission()) {
                download(url.trim(), userAgent, contentDisposition, mimeType, contentLength)
            }
        }

        loadWebView()
    }

    private fun loadWebView() {
        webView.loadUrl(webUrl)
    }

    private fun setProgressDialogVisibility(visible: Boolean) {
        if (visible) {
            loadingDialog.show()
        } else {
            loadingDialog.dismiss()
        }
    }

    private fun download(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        val folder = File(
            Environment.getExternalStorageDirectory().toString() + "/Download/Image"
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        Toast.makeText(requireContext(), "Download Started", Toast.LENGTH_SHORT).show()

        val request = DownloadManager.Request(Uri.parse(url))
        request.setMimeType(mimeType)
        val cookie = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader("cookie", cookie)
        request.addRequestHeader("User-Agent", userAgent)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        request.setTitle(fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "Image/$fileName"
        )
        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    Toast.makeText(requireContext(), "All permissions granted successfully", Toast.LENGTH_LONG).show()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(requireContext())
                    } else {
                        // here warning permission show
                        warningPermissionDialog(requireContext()) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }
}
